# Ontology History Backend

This service processes ontology history from supported Git repositories and stores calculated differences for the TIB Terminology Service frontend.

It currently supports three diff sources:

- Git syntax diff: raw textual diff between adjacent ontology file commits. For large diffs, the frontend should prefer the remote repository compare URL.
- ROBOT diff: semantic diff generated with ROBOT and stored in MongoDB.
- COnto diff: semantic change data generated with COnto and stored/queryable through Apache Fuseki.

## Local Startup

1. Create a local `.env` file in the repository root:

```bash
GITHUB_TOKEN=...
GITLAB_TOKEN=...
GIT_AACHEN_TOKEN=...
GIT_TIB_EU_TOKEN=...
GIT_ETSI_TOKEN=
```

Use personal access tokens. For public GitHub repositories, unauthenticated fallback is supported, but a token is recommended to avoid rate limits. For private/internal GitLab instances, use tokens with repository read/API access according to the instance policy.
`GIT_ETSI_TOKEN` is optional for public `labs.etsi.org` SAREF repositories; leave it empty unless ETSI access later requires authentication.

Supported Git hosts:

- `raw.githubusercontent.com`
- `gitlab.com`
- `git.rwth-aachen.de`
- `git.tib.eu`
- `labs.etsi.org/rep`

2. Start the services:

```bash
docker compose up -d --build
```

3. Open Fuseki:

```text
http://localhost:3030
```

Create a dataset named:

```text
test
```

The backend currently uses the hard-coded dataset name `test` in the OnDeT and COnto controllers.

4. Open Swagger:

```text
http://localhost:9090/swagger-ui/index.html
```

## Important Data Stores

- MongoDB stores ROBOT diffs, Git diffs, and batch job status.
- Fuseki stores COnto output and named graph data.
- Docker volumes persist both MongoDB and Fuseki data across container restarts.

## Frontend-Facing Endpoints

These endpoints are expected to be used by the TS frontend:

```text
GET /api/ondet/sdiffs/checkUrl?uri=<rawOntologyUrl>
GET /api/ondet/sdiffs/commits?uri=<rawOntologyUrl>
GET /api/ondet/sdiffs/latestVersion?uri=<rawOntologyUrl>
GET /api/ondet/sdiffs/{sha}?includeGitDiff=false
GET /api/ondet/sdiffs/{sha}?includeGitDiff=true&maxGitDiffBytes=1000000
```

Use `includeGitDiff=false` for normal commit-detail loading. This avoids sending very large raw Git diffs to the browser.

Use `includeGitDiff=true&maxGitDiffBytes=<bytes>` only when the frontend wants to load a small raw Git diff inline. If the stored diff is larger than `maxGitDiffBytes`, the backend leaves `gitDiff` empty and returns status metadata plus `gitDiffUrl`.

The commit-detail response includes:

```json
{
  "markdown": {},
  "difference": {
    "changes": [],
    "error": null
  },
  "gitDiff": "",
  "gitDiffUrl": "https://github.com/owner/repo/compare/base...head",
  "status": {
    "robot": {
      "status": "AVAILABLE",
      "message": "ROBOT diff is available"
    },
    "conto": {
      "status": "NOT_AVAILABLE",
      "message": "COnto diff was not stored for this commit. The diff may have failed, timed out, or produced no queryable result."
    },
    "git": {
      "status": "EXTERNAL_URL",
      "message": "Syntax diff can be opened in the source repository compare view.",
      "url": "https://github.com/owner/repo/compare/base...head",
      "sizeBytes": 14500000,
      "inlineRecommended": false
    }
  }
}
```

Frontend behavior:

- If `status.git.inlineRecommended` is `true`, the frontend may request `includeGitDiff=true&maxGitDiffBytes=1000000` and render the returned `gitDiff` inline.
- If `status.git.status` is `EXTERNAL_URL` or `status.git.inlineRecommended` is `false`, open `status.git.url` or `gitDiffUrl` in a new browser tab.
- Only request raw Git diffs with a frontend-owned byte limit.
- Do not treat empty ROBOT/COnto data as proof that there were no changes. Use the `status` object for user-facing messages.

## Maintenance Batch Processing

Batch processing is intended for curators/admins, not normal TS frontend users.

The batch trigger and status endpoints are intentionally hidden from Swagger, but remain callable for maintenance scripts:

```text
POST /api/ondet/sdiffs/createBatch
GET /api/ondet/sdiffs/jobStatus/{jobId}
```

Recommended operational model:

- Run batch processing manually once per quarter.
- Fetch raw ontology URLs from the TS API.
- Submit those URLs to the batch endpoint.
- Poll job status until it reaches `COMPLETED` or `COMPLETED_WITH_FAILURES`.
- Review failed/skipped ontologies.
- Retry only failed/skipped ontologies after investigation.

Example:

```bash
curl -X POST 'http://localhost:9090/api/ondet/sdiffs/createBatch' \
  -H 'Content-Type: application/json' \
  -d '[
    "https://raw.githubusercontent.com/oeg-upm/bimerr-material-properties/refs/heads/master/ontology/mat.ttl",
    "https://raw.githubusercontent.com/tdwg/abcd/master/ontology/abcd_concepts.owl",
    "https://raw.githubusercontent.com/Sumon-tun/SARGON2/refs/heads/master/sargon2.owl"
  ]'
```

Status:

```bash
curl 'http://localhost:9090/api/ondet/sdiffs/jobStatus/<jobId>'
```

Local script:

```bash
scripts/run-batch-processing.sh --input urls.txt
```

Fetch URLs from the TS API and process them:

```bash
scripts/run-batch-processing.sh --fetch-ts
```

Prepare/filter URLs without submitting a batch:

```bash
scripts/run-batch-processing.sh --fetch-ts --prepare-only
```

Process a prepared list in chunks of 10:

```bash
scripts/run-batch-processing.sh \
  --input-json batch-results/ontology-urls-all-<timestamp>.json \
  --chunk-size 10 \
  --chunk-index 0
```

The script writes a chunk manifest and prints the command for the next chunk when the current one finishes. Increase `--chunk-index` by one for each next chunk. For example, `--chunk-index 1` processes URLs 11-20.

The script writes the submitted URL list, final status JSON, non-added ontology TSV report, and skipped URL report to `batch-results/`.
It also writes a failure summary TSV for quick review of common causes.

Batch COnto processing is disabled by default because COnto can block large quarterly runs:

```properties
ondet.batch.conto.enabled=${ONDET_BATCH_CONTO_ENABLED:false}
ondet.batch.conto.max-diff-pairs=${ONDET_BATCH_CONTO_MAX_DIFF_PAIRS:5}
ondet.batch.conto.max-pair-raw-bytes=${ONDET_BATCH_CONTO_MAX_PAIR_RAW_BYTES:5000000}
```

Configure these values through `.env` or deployment environment variables:

```bash
ONDET_BATCH_CONTO_ENABLED=false
ONDET_BATCH_CONTO_MAX_DIFF_PAIRS=5
ONDET_BATCH_CONTO_MAX_PAIR_RAW_BYTES=5000000
```

For a full curator-controlled ingestion run that includes Git diff, ROBOT, and COnto, set `ONDET_BATCH_CONTO_ENABLED=true` and raise the two limits deliberately for that environment. Keep the default disabled setting for safer unattended deployments.

ROBOT can use an XML catalog to resolve `owl:imports` to local files instead of downloading common imports repeatedly during a batch. This is recommended for quarterly runs because public import URLs can rate-limit the backend, for example with HTTP `429`.

Enable it by setting:

```properties
ondet.robot.catalog.path=/app/robot-catalog/catalog-v001.xml
```

or in `.env`:

```bash
ROBOT_CATALOG_PATH=/app/robot-catalog/catalog-v001.xml
```

Mount the catalog directory into the backend container, for example:

```yaml
services:
  history-back:
    volumes:
      - ./robot-catalog:/app/robot-catalog:ro
```

For local testing, the repository also includes an optional Compose override:

```bash
docker compose -f docker-compose.yml -f docker-compose.robot-catalog.yml up -d --build history-back
```

Example `catalog-v001.xml`:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<catalog prefer="public" xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog">
  <uri name="http://www.w3.org/2004/02/skos/core#" uri="imports/skos.rdf"/>
  <uri name="http://www.w3.org/2004/02/skos/core" uri="imports/skos.rdf"/>
</catalog>
```

The referenced local files must exist next to the catalog, for example `robot-catalog/imports/skos.rdf`. Keep the catalog small and curator-managed at first: add entries only for stable imports that are repeatedly failing or rate-limited in batch logs.

When COnto is skipped but Git/ROBOT processing completes, the ontology is marked `ADDED_WITH_WARNINGS`.

Batch job statuses:

- `RUNNING`
- `COMPLETED`
- `COMPLETED_WITH_FAILURES`
- `FAILED`

Per-ontology statuses:

- `PROCESSING`
- `ADDED`
- `ADDED_WITH_WARNINGS`
- `NO_COMPARABLE_VERSIONS`
- `UNSUPPORTED_HOST`
- `FAILED`

`NO_COMPARABLE_VERSIONS` usually means the ontology file has fewer than two comparable commits, so no adjacent commit pair exists for diff calculation. GitHub release pages are currently not processed; replace those TS `versioned_url` values with raw ontology file URLs before running a batch.

## Startup Scheduler

`OntologyScheduler` contains older startup/scheduled processing methods, but the annotations are currently commented out. Keep this disabled unless the batch/resume behavior has been made robust enough for long-running unattended production work.

Processing all TS ontologies during application startup is not recommended because:

- startup becomes slow and fragile
- one large ontology can delay the whole service
- failures are harder to inspect
- curators need explicit status/retry reporting

Use the maintenance batch workflow instead.

## COnto Notes

COnto uses the bundled JAR:

```text
lib/ContoDiff-1.0-SNAPSHOT-shaded.jar
```

The Docker build installs this JAR into the local Maven repository as:

```text
org.dbs.leipzig:conto:1.0
```

The bundled JAR is not the plain upstream Leipzig COnto CLI. It is based on COnto/GOMMA/webdifftool code, but includes TIB-specific additions such as:

- `org.ContoDiffMain.makeContoDiff(DiffContext, String)`, which is the method called by this backend
- RDF/N-Quads generation for `output.ttl` and `all_diffs.nq`
- `eu.tib.contodiff.*` classes
- `org.webdifftool.client.model.*` classes used by the backend integration

The original upstream repository exposes a CLI that writes a compact text diff, but it does not provide the backend-facing `makeContoDiff` API or the RDF/N-Quads output used here. Therefore, do not replace the bundled JAR with the original upstream JAR as a drop-in dependency.

Known COnto limitation: COnto may successfully parse and compare two ontology versions but still produce no concrete change graph:

```text
output.ttl > 0 bytes
all_diffs.nq = 0 bytes
```

The backend stores this as:

```text
EMPTY_CHANGE_GRAPH
```

This does not mean the ontology had no changes. It means COnto did not represent the edit in its change model. ROBOT and Git diff may still show meaningful changes for the same commit.

Common examples observed during local testing:

- ontology annotations and metadata changes
- version IRI / version info / date changes
- syntax or serialization-only edits
- some property/schema edits that ROBOT reports as added/removed axioms

Other COnto diagnostic stages:

- `CONTO_PARSE_ERROR`: COnto/OWLAPI could not parse one historical ontology version.
- `EMPTY_CHANGE_GRAPH`: COnto ran, but `all_diffs.nq` was empty.
- `UNLINKED_CHANGE_GRAPH`: COnto produced N-Quads, but they could not be linked to commit metadata. This should be rare after the commit-SHA SPARQL linking fix.
- `QUERY_VERIFICATION`: COnto output was uploaded, but the backend query found no displayable changes.
- `FUSEKI_UPLOAD`: generated COnto output could not be uploaded to Fuseki.
- `OUTPUT_VALIDATION`: expected generated files were missing.
- `CONTO_EXECUTION`: non-parse COnto runtime failure.

Manual A/B check against original COnto:

```bash
mkdir -p /tmp/conto-compare
git clone --depth 1 https://github.com/dbs-leipzig/conto_diff.git /tmp/conto-compare/original
mvn -q -f /tmp/conto-compare/original/pom.xml package -DskipTests
mvn -q -f /tmp/conto-compare/original/pom.xml dependency:build-classpath \
  -Dmdep.outputFile=/tmp/conto-compare/original/cp.txt

CP="/tmp/conto-compare/original/target/classes:$(cat /tmp/conto-compare/original/cp.txt)"

cd /tmp/conto-compare/original
java -cp "$CP" org.ContoDiffMain \
  -oa /tmp/conto-compare/cases/example/left.ttl \
  -ob /tmp/conto-compare/cases/example/right.ttl \
  -o /tmp/conto-compare/cases/example/original-output.txt \
  -d
```

Run the original CLI from the original COnto checkout so relative paths such as `rules/ChangeActions.xml` resolve correctly. If you run it from this backend repository, it may create local H2 files such as `COntoDiff.mv.db` and `COntoDiff.trace.db`; do not commit those.

Known A/B result:

- `mat.ttl` commit pair `a93ec3f8cdd56cf37b492b16ba9320d49e4bace8 -> 39807b72e1384f32b3b0d9c91153201bca642689`
- Backend/TIB COnto: `EMPTY_CHANGE_GRAPH`, `all_diffs.nq = 0`
- Original COnto CLI: `original-output.txt = 0 bytes`
- Conclusion: this is a COnto model limitation, not a backend/Fuseki/SPARQL bug.

Use this process before considering any future COnto replacement or fork update.

Frontend/user-facing guidance: show COnto `NOT_AVAILABLE` messages as a limitation of this diff source, not as “no changes”. ROBOT and Git diff are the fallback evidence for changes in these cases.

## Large Ontologies

Some ontology commits produce very large diffs. GitHub itself may refuse to render these and show a message such as “diff is too large”.

For these cases:

- backend should still store whatever ROBOT/Git/COnto output is successfully generated
- frontend should avoid downloading raw Git diffs by default
- frontend may render small Git diffs inline by requesting `includeGitDiff=true&maxGitDiffBytes=1000000`
- frontend should open the remote compare URL when the Git diff is too large
- missing ROBOT/COnto output should be shown as “not available”, not “no changes”

## Useful Checks

Compile backend:

```bash
mvn -q -DskipTests package
```

Rebuild backend container:

```bash
docker compose up -d --build history-back
```

Check lightweight commit details:

```bash
curl -s 'http://localhost:9090/api/ondet/sdiffs/<sha>?includeGitDiff=false'
```

Inspect Fuseki manually:

```sparql
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT * WHERE {
  ?sub ?pred ?obj .
}
LIMIT 10
```
