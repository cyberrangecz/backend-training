alter table training_definition drop column show_stepper_bar;
alter table training_instance add column show_stepper_bar boolean not null default true,