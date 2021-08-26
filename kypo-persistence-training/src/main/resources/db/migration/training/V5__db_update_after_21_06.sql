alter table question_answers alter column answer type varchar (1023);
alter table if exists game_level rename to training_level;

do
$$
    begin
        if exists
            ( select * from information_schema.columns where table_name='training_level' and column_name='flag' )
        then
            alter table training_level rename column flag to answer;
        end if;

        if exists
            ( select * from information_schema.columns where table_name='training_level' and column_name='incorrect_flag_limit' )
        then
            alter table training_level rename column incorrect_flag_limit to incorrect_answer_limit;
        end if;

        if exists
            ( select * from information_schema.columns where table_name='training_run' and column_name='incorrect_flag_count' )
        then
            alter table training_run rename column incorrect_flag_count to incorrect_answer_count;
        end if;

        if exists
            ( select * from information_schema.columns where table_name='training_run' and column_name='total_game_score' )
        then
            alter table training_run rename column total_game_score to total_training_score;
        end if;

        if exists
            ( select * from information_schema.columns where table_name='hint' and column_name='game_level_id' )
        then
            alter table hint rename column game_level_id to training_level_id;
        end if;

        if exists
            ( select * from information_schema.columns where table_name='hint_info' and column_name='game_level_id' )
        then
            alter table hint_info rename column game_level_id to training_level_id;
        end if;

        if exists
            ( select * from information_schema.columns where table_name='attachment' and column_name='game_level_id' )
        then
            alter table attachment rename column game_level_id to training_level_id;
        end if;
    end;
$$;

alter table training_level add column if not exists answer_variable_name varchar(255);
alter table training_level alter column answer drop not null;
alter table training_definition add column if not exists variant_sandboxes boolean default (false);
alter table training_definition add column if not exists last_edited_by varchar(127) not null default '';
alter table training_instance add column if not exists last_edited timestamp not null default CURRENT_TIMESTAMP;
alter table training_instance add column if not exists last_edited_by varchar(127) not null default '';