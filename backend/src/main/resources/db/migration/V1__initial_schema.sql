create table simulation_run (id uuid primary key, scenario varchar(64) not null check (scenario in ('NORMAL_ORDER','DUPLICATE_ORDER','PAYMENT_FAILURE','TIMEOUT_WITH_IDEMPOTENCY')), created_at timestamp(6) with time zone not null, status varchar(32) not null check(status in ('FAILED','COMPLETED')));
create index idx_run_created on simulation_run(created_at);
create table trace_event (id uuid primary key, run_id uuid not null references simulation_run(id), sequence_number integer not null check(sequence_number > 0), payload text not null, unique(run_id,sequence_number));
create table simulated_order (id uuid primary key, run_id uuid not null references simulation_run(id), logical_order_id varchar(255) not null, idempotency_key varchar(255) not null, status varchar(32) not null);
create index idx_order_run on simulated_order(run_id);
create index idx_order_logical on simulated_order(logical_order_id);
create table result_artifact (id uuid primary key, run_id uuid not null references simulation_run(id), kind varchar(32) not null check(kind in ('INVESTIGATION','VERIFICATION')), created_at timestamp(6) with time zone not null, payload text not null);
create index idx_artifact_run_kind on result_artifact(run_id,kind,created_at);
