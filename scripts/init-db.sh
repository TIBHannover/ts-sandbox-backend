#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL

    CREATE SEQUENCE ont_id_seq;

    CREATE TABLE public.processed_ontology (
    	id bigint NOT NULL,
    	ontology_id varchar(255) NOT NULL,
    	uri varchar(255) NULL,
    	title varchar(255) NULL,
    	created_at timestamptz NULL,
    	update_at timestamptz NULL,
    	CONSTRAINT processed_ontology_pkey PRIMARY KEY (id),
    	CONSTRAINT uk_ontology_id UNIQUE (ontology_id)
    );

    CREATE TABLE public.properties (
    	id bigint NOT NULL,
    	properties varchar(255) NULL
    );

    CREATE TABLE public.collection (
    	id bigint NOT NULL,
    	collection varchar(255) NULL
    );

    CREATE TABLE public.imports (
    	id bigint NOT NULL,
    	imports varchar(255) NULL
    );

    CREATE TABLE public.namespaces (
    	id bigint NOT NULL,
    	namespaces varchar(255) NULL
    );

    CREATE TABLE public.classes (
    	id bigint NOT NULL,
    	classes varchar(255) NULL
    );

    CREATE TABLE public.individuals (
        	id bigint NOT NULL,
        	individuals varchar(255) NULL
        );

    ALTER TABLE public.properties
    ADD CONSTRAINT fk_properties FOREIGN KEY (id) REFERENCES public.processed_ontology(id);

    ALTER TABLE public.classes
    ADD CONSTRAINT fk_classes FOREIGN KEY (id) REFERENCES public.processed_ontology(id);

    ALTER TABLE public.collection
    ADD CONSTRAINT fk_collection FOREIGN KEY (id) REFERENCES public.processed_ontology(id);

    ALTER TABLE public.imports
    ADD CONSTRAINT fk_imports FOREIGN KEY (id) REFERENCES public.processed_ontology(id);

    ALTER TABLE public.namespaces
    ADD CONSTRAINT fk_namespaces FOREIGN KEY (id) REFERENCES public.processed_ontology(id);

    ALTER TABLE public.individuals
        ADD CONSTRAINT fk_individuals FOREIGN KEY (id) REFERENCES public.processed_ontology(id);


EOSQL