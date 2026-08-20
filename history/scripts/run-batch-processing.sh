#!/usr/bin/env bash
set -euo pipefail

BACKEND_URL="${BACKEND_URL:-http://localhost:9090}"
TS_API_URL="${TS_API_URL:-https://api.terminology.tib.eu/api/v2/ontologies?size=1000}"
POLL_SECONDS="${POLL_SECONDS:-30}"
INPUT_FILE=""
INPUT_JSON=""
OUTPUT_DIR="${OUTPUT_DIR:-batch-results}"
PREPARE_ONLY=false
CHUNK_SIZE=0
CHUNK_INDEX=0

usage() {
  cat <<'USAGE'
Run an OnDeT maintenance batch job.

Usage:
  scripts/run-batch-processing.sh --input urls.txt
  scripts/run-batch-processing.sh --input-json batch-results/ontology-urls-YYYYMMDDTHHMMSSZ.json
  scripts/run-batch-processing.sh --fetch-ts
  scripts/run-batch-processing.sh --fetch-ts --prepare-only
  scripts/run-batch-processing.sh --input-json batch-results/ontology-urls-YYYYMMDDTHHMMSSZ.json --chunk-size 10 --chunk-index 0

Environment:
  BACKEND_URL   Default: http://localhost:9090
  TS_API_URL    Default: https://api.terminology.tib.eu/api/v2/ontologies?size=1000
  POLL_SECONDS  Default: 30
  OUTPUT_DIR    Default: batch-results

Requirements:
  curl, jq

Input file format:
  One raw ontology URL per line. Empty lines and lines starting with # are ignored.

Chunking:
  --chunk-size 10 submits only 10 URLs from the prepared list.
  --chunk-index 0 submits the first chunk, --chunk-index 1 submits the second chunk, etc.
  The script prints the next chunk command after each run.
USAGE
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --input)
      INPUT_FILE="${2:-}"
      shift 2
      ;;
    --input-json)
      INPUT_JSON="${2:-}"
      shift 2
      ;;
    --fetch-ts)
      INPUT_FILE=""
      INPUT_JSON=""
      shift
      ;;
    --prepare-only)
      PREPARE_ONLY=true
      shift
      ;;
    --chunk-size)
      CHUNK_SIZE="${2:-}"
      shift 2
      ;;
    --chunk-index)
      CHUNK_INDEX="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1
      ;;
  esac
done

require_command curl
require_command jq

if [[ -n "$INPUT_FILE" && -n "$INPUT_JSON" ]]; then
  echo "Use either --input or --input-json, not both." >&2
  exit 1
fi

if ! [[ "$CHUNK_SIZE" =~ ^[0-9]+$ ]]; then
  echo "--chunk-size must be a non-negative integer." >&2
  exit 1
fi

if ! [[ "$CHUNK_INDEX" =~ ^[0-9]+$ ]]; then
  echo "--chunk-index must be a non-negative integer." >&2
  exit 1
fi

mkdir -p "$OUTPUT_DIR"
timestamp="$(date -u +%Y%m%dT%H%M%SZ)"
urls_json="$OUTPUT_DIR/ontology-urls-$timestamp.json"
all_urls_json="$OUTPUT_DIR/ontology-urls-all-$timestamp.json"
submit_response="$OUTPUT_DIR/batch-submit-$timestamp.json"
status_file="$OUTPUT_DIR/batch-status-$timestamp.json"
skipped_urls_file="$OUTPUT_DIR/skipped-urls-$timestamp.tsv"
not_added_file="$OUTPUT_DIR/batch-not-added-$timestamp.tsv"
chunk_manifest_file="$OUTPUT_DIR/batch-chunk-$timestamp.json"
failure_summary_file="$OUTPUT_DIR/batch-failure-summary-$timestamp.tsv"

write_not_added_report() {
  if [[ -f "$status_file" ]]; then
    jq -r '
      .results[]
      | select(.status != "ADDED" and .status != "ADDED_WITH_WARNINGS")
      | [.status, .uri, (.message // "")] | @tsv
    ' "$status_file" > "$not_added_file"
  fi
}

write_failure_summary_report() {
  if [[ -f "$status_file" ]]; then
    jq -r '
      def reason:
        if .status == "NO_COMPARABLE_VERSIONS" then "NO_COMPARABLE_VERSIONS"
        elif .status == "UNSUPPORTED_HOST" then "UNSUPPORTED_URL"
        elif .status == "FAILED" then
          (.message // "")
          | if test("IMPORT_HTTP_ERROR") then "IMPORT_HTTP_ERROR"
          elif test("IMPORT_PARSE_ERROR") then "IMPORT_PARSE_ERROR"
          elif test("ONTOLOGY_PARSE_ERROR") then "ONTOLOGY_PARSE_ERROR"
          elif test("GitLab project lookup") then "GITLAB_PROJECT_LOOKUP"
          elif test("unsupported raw Git file URL|UNSUPPORTED_HOST") then "UNSUPPORTED_URL"
          elif test("No comparable|No adjacent|fewer than two comparable") then "NO_COMPARABLE_VERSIONS"
          elif test("COnto skipped") then "CONTO_SKIPPED"
          elif length > 0 then "OTHER"
          else "NO_MESSAGE"
          end
        else .status
        end;
      [.results[] | select(.status != "ADDED" and .status != "ADDED_WITH_WARNINGS") | reason]
      | group_by(.)
      | map([.[0], length] | @tsv)
      | .[]
    ' "$status_file" > "$failure_summary_file"
  fi
}

normalize_filter_jq='
  def trim: gsub("^\\s+|\\s+$"; "");
  def github_to_raw:
    capture("^https://github[.]com/(?<owner>[^/]+)/(?<repo>[^/]+)/(raw|blob)/(?<ref>refs/(heads|tags)/[^/]+/[^/]+|[^/]+)/(?<path>.+)$")
    | "https://raw.githubusercontent.com/\(.owner)/\(.repo)/\(.ref)/\(.path)";
  def add_scheme:
    if test("^labs[.]etsi[.]org/") then
      "https://\(.)"
    else
      .
    end;
  def normalize:
    add_scheme
    | if test("^https://github[.]com/.+/(raw|blob)/") then
      try github_to_raw catch .
    else
      .
    end;
  def supported:
    test("^https://raw[.]githubusercontent[.]com/[^/]+/[^/]+/.+/.+")
    or test("^https://(gitlab[.]com|git[.]rwth-aachen[.]de|git[.]tib[.]eu|labs[.]etsi[.]org)/.+/-/raw/.+");
  map(trim | select(length > 0) | normalize) | unique
'

if [[ -n "$INPUT_JSON" ]]; then
  if [[ ! -f "$INPUT_JSON" ]]; then
    echo "Input JSON file does not exist: $INPUT_JSON" >&2
    exit 1
  fi
  jq "$normalize_filter_jq" "$INPUT_JSON" > "$urls_json"
elif [[ -n "$INPUT_FILE" ]]; then
  if [[ ! -f "$INPUT_FILE" ]]; then
    echo "Input file does not exist: $INPUT_FILE" >&2
    exit 1
  fi
  jq -Rn '
    [inputs
      | select(test("^\\s*(#|$)") | not)
      | gsub("^\\s+|\\s+$"; "")]
  ' "$INPUT_FILE" | jq "$normalize_filter_jq" > "$urls_json"
else
  echo "Fetching ontology URLs from TS API: $TS_API_URL"
  curl -fsS "$TS_API_URL" \
    | jq '[.elements[] | select(.versioned_url != null) | .versioned_url]' \
    | jq "$normalize_filter_jq" \
    > "$urls_json"
fi

cp "$urls_json" "$all_urls_json"

jq -r '
  .[]
  | select(
      (test("^https://raw[.]githubusercontent[.]com/[^/]+/[^/]+/.+/.+") or
       test("^https://(gitlab[.]com|git[.]rwth-aachen[.]de|git[.]tib[.]eu|labs[.]etsi[.]org)/.+/-/raw/.+"))
      | not
    )
  | ["SKIPPED_UNSUPPORTED_URL", .] | @tsv
' "$urls_json" > "$skipped_urls_file"

jq '
  [
    .[]
    | select(
        test("^https://raw[.]githubusercontent[.]com/[^/]+/[^/]+/.+/.+") or
        test("^https://(gitlab[.]com|git[.]rwth-aachen[.]de|git[.]tib[.]eu|labs[.]etsi[.]org)/.+/-/raw/.+")
      )
  ]
' "$urls_json" > "$urls_json.tmp"
mv "$urls_json.tmp" "$urls_json"

total_count="$(jq 'length' "$urls_json")"
if [[ "$total_count" -eq 0 ]]; then
  echo "No ontology URLs found." >&2
  exit 1
fi

if [[ "$CHUNK_SIZE" -gt 0 ]]; then
  chunk_start=$((CHUNK_INDEX * CHUNK_SIZE))
  chunk_end=$((chunk_start + CHUNK_SIZE))
  jq --argjson start "$chunk_start" --argjson stop "$chunk_end" 'to_entries | map(select(.key >= $start and .key < $stop) | .value)' "$urls_json" > "$urls_json.tmp"
  mv "$urls_json.tmp" "$urls_json"
fi

count="$(jq 'length' "$urls_json")"
if [[ "$count" -eq 0 ]]; then
  echo "Selected chunk is empty. total=$total_count chunkSize=$CHUNK_SIZE chunkIndex=$CHUNK_INDEX" >&2
  exit 1
fi

if [[ "$CHUNK_SIZE" -gt 0 ]]; then
  total_chunks=$(( (total_count + CHUNK_SIZE - 1) / CHUNK_SIZE ))
else
  total_chunks=1
fi

jq -n \
  --arg timestamp "$timestamp" \
  --arg urlsFile "$urls_json" \
  --arg allUrlsFile "$all_urls_json" \
  --argjson total "$total_count" \
  --argjson submitted "$count" \
  --argjson chunkSize "$CHUNK_SIZE" \
  --argjson chunkIndex "$CHUNK_INDEX" \
  --argjson totalChunks "$total_chunks" \
  '{
    timestamp: $timestamp,
    urlsFile: $urlsFile,
    allUrlsFile: $allUrlsFile,
    totalUrls: $total,
    submittedUrls: $submitted,
    chunkSize: $chunkSize,
    chunkIndex: $chunkIndex,
    totalChunks: $totalChunks
  }' > "$chunk_manifest_file"

skipped_count="$(wc -l < "$skipped_urls_file")"
echo "Prepared $total_count supported ontology URL(s). Skipped $skipped_count unsupported URL(s)."
if [[ "$CHUNK_SIZE" -gt 0 ]]; then
  echo "Selected chunk $((CHUNK_INDEX + 1))/$total_chunks: $count URL(s)."
fi
echo "Supported URL list saved to $urls_json"
echo "Full supported URL list saved to $all_urls_json"
echo "Skipped URL report saved to $skipped_urls_file"
echo "Chunk manifest saved to $chunk_manifest_file"

if [[ "$PREPARE_ONLY" == "true" ]]; then
  exit 0
fi

echo "Submitting $count ontology URL(s) to $BACKEND_URL"
curl -fsS -X POST "$BACKEND_URL/api/ondet/sdiffs/createBatch" \
  -H 'Content-Type: application/json' \
  --data @"$urls_json" \
  | tee "$submit_response" >/dev/null

job_id="$(jq -r '.jobId // empty' "$submit_response")"
if [[ -z "$job_id" ]]; then
  echo "Backend did not return a jobId. Response saved to $submit_response" >&2
  exit 1
fi

echo "Batch job started: $job_id"
echo "Polling every $POLL_SECONDS second(s). Status will be saved to $status_file"

while true; do
  curl -fsS "$BACKEND_URL/api/ondet/sdiffs/jobStatus/$job_id" \
    | tee "$status_file" >/dev/null
  write_not_added_report
  write_failure_summary_report

  status="$(jq -r '.status // "UNKNOWN"' "$status_file")"
  processed="$(jq -r '.processed // 0' "$status_file")"
  total="$(jq -r '.total // 0' "$status_file")"
  added="$(jq -r '.added // 0' "$status_file")"
  not_added="$(jq -r '.notAdded // 0' "$status_file")"

  echo "$(date -u +%Y-%m-%dT%H:%M:%SZ) status=$status processed=$processed/$total added=$added notAdded=$not_added"

  case "$status" in
    COMPLETED|COMPLETED_WITH_FAILURES|FAILED)
      break
      ;;
  esac

  sleep "$POLL_SECONDS"
done

write_not_added_report
write_failure_summary_report

echo "Final status saved to $status_file"
echo "Non-added ontology report saved to $not_added_file"
echo "Failure summary report saved to $failure_summary_file"
echo "Skipped URL report saved to $skipped_urls_file"

if [[ "$CHUNK_SIZE" -gt 0 && "$((CHUNK_INDEX + 1))" -lt "$total_chunks" ]]; then
  next_chunk=$((CHUNK_INDEX + 1))
  echo "Next chunk command:"
  echo "  scripts/run-batch-processing.sh --input-json $all_urls_json --chunk-size $CHUNK_SIZE --chunk-index $next_chunk"
fi
