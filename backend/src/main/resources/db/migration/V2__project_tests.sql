create table project_test_job (id uuid primary key, created_at timestamp(6) with time zone not null, payload text not null);
create index idx_project_test_created on project_test_job(created_at);
