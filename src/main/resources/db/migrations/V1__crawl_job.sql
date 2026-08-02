CREATE TABLE public.crawl_jobs (
    id uuid NOT NULL,
    initial_url text NOT NULL,
    max_depth int NULL,
    status text NOT NULL,
    result jsonb NULL,
    CONSTRAINT PK_crawl_jobs PRIMARY KEY (id)
);
CREATE INDEX IDX_crawl_jobs_initial_url ON public.crawl_jobs (initial_url);
CREATE INDEX IDX_crawl_jobs_status ON public.crawl_jobs (status);
