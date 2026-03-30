-- Ensure at most one RUNNING training run per training instance uses a given sandbox allocation.
-- Prevents race where two users are assigned the same pool allocation when accessing a managed instance.
CREATE UNIQUE INDEX training_run_one_allocation_per_instance
ON training_run (training_instance_id, sandbox_instance_allocation_id)
WHERE state = 'RUNNING' AND sandbox_instance_allocation_id IS NOT NULL;
