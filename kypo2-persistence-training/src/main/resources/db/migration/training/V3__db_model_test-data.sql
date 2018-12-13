INSERT INTO pre_hook(id) VALUES (1);
INSERT INTO post_hook(id) VALUES (1);
INSERT INTO pre_hook(id) VALUES (2);
INSERT INTO post_hook(id) VALUES (2);
INSERT INTO pre_hook(id) VALUES (3);
INSERT INTO post_hook(id) VALUES (3);
INSERT INTO pre_hook(id) VALUES (4);
INSERT INTO post_hook(id) VALUES (4);
INSERT INTO pre_hook(id) VALUES (5);
INSERT INTO post_hook(id) VALUES (5);
INSERT INTO pre_hook(id) VALUES (6);
INSERT INTO post_hook(id) VALUES (6);

INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (1, 20, 2, 'Game Level1', 1, 1);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (2, 70, 3, 'Info Level1', 2, 2);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (3, 50, null, 'Assessment Level1', 3, 3);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (4, 55, 5, 'Game Level2', 4, 4);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (5, 13, 6, 'Info Level2', 5, 5);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (6, 75, null, 'Assessment Level2', 6, 6);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (7, 0, 8, 'Info Level Test', 3, 3);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (8, 70, 9, 'Game Level Test', 2, 2);
INSERT INTO abstract_level(id, max_score, next_level, title, post_hook_id, pre_hook_id) VALUES (9, 100, null, 'Assessment Level Test', 1, 1);

INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (1, null, 25, 5, 'Play me', 'secretFlag', 'This is how you do it', true );
INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (4, null, 60, 3, 'Unsolvable problem', 'jibberish', 'Not sure yet', false);
INSERT INTO game_level(id, attachments, estimated_duration, incorrect_flag_limit, content, flag, solution, solution_penalized) VALUES (8, null, 25, 5, 'Play me', 'secretFlag', 'correct flag', true);

INSERT INTO info_level(id, content) VALUES (2, 'Informational stuff');
INSERT INTO info_level(id, content) VALUES (5,'Potatoes are not poisonous');
INSERT INTO info_level(id, content) VALUES (7, 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean vel tellus id orci interdum pulvinar et eu nisi. Suspendisse consequat, metus vel tempus placerat, eros nulla gravida enim, at suscipit nunc ex sit amet purus. Morbi scelerisque felis eget scelerisque ultricies. Aenean maximus, eros ac convallis tempus, ipsum ipsum venenatis magna, ac ullamcorper odio felis id augue. Donec tempus quis mauris quis sollicitudin. Donec et lorem porttitor, vulputate neque ut, sodales arcu. Phasellus iaculis dolor vel tincidunt vestibulum. Etiam dui neque, congue id aliquet et, blandit at diam. Ut purus orci, dapibus semper lobortis in, placerat id augue. Nam varius, ex sit amet viverra molestie, metus nibh ornare diam, in volutpat risus ligula eget ipsum. ');

INSERT INTO assessment_level(id, assessment_type, instructions, questions) VALUES (3, 'TEST', 'Fill me up', 'What is my mothers name?');
INSERT INTO assessment_level(id, assessment_type, instructions, questions) VALUES (6, 'QUESTIONNAIRE', 'No rush', '...?');
INSERT INTO assessment_level(id, assessment_type, instructions, questions) VALUES (9, 'TEST', 'Fill me up', '[{"question_type":"FFQ","text":"Write name of one malicious software?","points":6,"penalty":3,"order":0,"answer_required":true,"correct_choices":["viruses","trojans","worms","bots"]},{"question_type":"MCQ","text":"Among the following choices, select all the possible methods of prevention against an unwanted file upload.","points":4,"penalty":2,"order":1,"answer_required":true,"choices":[{"order":0,"text":"whitelisting file extensions","is_correct":true},{"order":1,"text":"limiting maximum file size","is_correct":true},{"order":2,"text":"using database triggers","is_correct":false},{"order":3,"text":"saving data to an NTFS volume","is_correct":false}]},{"question_type":"EMI","text":"Connect the following exemplary situations with the corresponding type of password attack.","points":3,"penalty":1,"order":2,"answer_required":true,"choices":[{"order":0,"text":"trying all possible alphanumeric combinations of 8 characters","pair":6},{"order":1,"text":"trying common words of English language","pair":4},{"order":2,"text":"looking up the value of a hashed password","pair":7},{"order":3,"text":"tricking a user into giving away his password by posing as a service administrator","pair":5},{"order":4,"text":"dictionary attack","pair":1},{"order":5,"text":"social engineering","pair":3},{"order":6,"text":"brute force attack","pair":0},{"order":7,"text":"rainbow table attack","pair":2}]}]');

INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (1, 'Very good advice', 'Hint1', 10, 1);
INSERT INTO hint(id, content, title, hint_penalty, game_level_id) VALUES (2, 'Very bad advice', 'Hint2', 6, 4);

INSERT INTO sandbox_definition_ref(id, sandbox_definition_ref) VALUES (1, 1);
INSERT INTO sandbox_definition_ref(id, sandbox_definition_ref) VALUES (2, 2);
INSERT INTO sandbox_definition_ref(id, sandbox_definition_ref) VALUES (3, 3);

INSERT INTO author_ref(id, author_ref_login) VALUES (1, 'Designer1');
INSERT INTO author_ref(id, author_ref_login) VALUES (2, 'Designer2');
INSERT INTO author_ref(id, author_ref_login) VALUES (3, 'Designer3');

INSERT INTO training_definition(id, description, outcomes, prerequisities, state, title, sand_box_definition_ref_id, starting_level, show_stepper_bar) VALUES (1, 'Released training definition', null, null, 'RELEASED', 'TrainingDefinition1', 1, 1, true);
INSERT INTO training_definition(id, description, outcomes, prerequisities, state, title, sand_box_definition_ref_id, starting_level, show_stepper_bar) VALUES (2, 'Unreleased training definition', null, null, 'UNRELEASED', 'TrainingDefinition2', 2, 4, false);
INSERT INTO training_definition(id, description, outcomes, prerequisities, state, title, sand_box_definition_ref_id, starting_level, show_stepper_bar) VALUES (3, 'Released training definition2', null, null, 'RELEASED', 'TrainingDefinition2', 3, 7, true);

INSERT INTO training_definition_author_ref(training_definition_id, author_ref_id) VALUES (1, 1);
INSERT INTO training_definition_author_ref(training_definition_id, author_ref_id) VALUES (2, 2);
INSERT INTO training_definition_author_ref(training_definition_id, author_ref_id) VALUES (3, 3);

INSERT INTO training_definition_sandbox_definition_ref(training_definition_id, sandbox_definition_ref_id) VALUES (1, 1);
INSERT INTO training_definition_sandbox_definition_ref(training_definition_id, sandbox_definition_ref_id) VALUES (2, 2);

INSERT INTO training_instance(id, pool_size, training_definition_id, start_time, end_time, password, title) VALUES (1, 5, 1, '2016-10-19 10:23:54+02', '2017-10-19 10:23:54+02', 'pass-1235', 'Concluded Instance');
INSERT INTO training_instance(id, pool_size, training_definition_id, start_time, end_time, password, title) VALUES (2, 8, 1, '2016-10-19 10:23:54+02', '2022-10-19 10:23:54+02', 'hello-6578', 'Current Instance');
INSERT INTO training_instance(id, pool_size, training_definition_id, start_time, end_time, password, title) VALUES (3, 25, 1, '2020-10-19 10:23:54+02', '2024-10-19 10:23:54+02', 'keyword-9999', 'Future Instance');
INSERT INTO training_instance(id, pool_size, training_definition_id, start_time, end_time, password, title) VALUES (4, 25, 3, '2020-10-19 10:23:54+02', '2024-10-19 10:23:54+02', 'keyword-1111', 'Future Instance');

INSERT INTO password(id, password) VALUES (1, 'pass-1235');
INSERT INTO password(id, password) VALUES (2, 'hello-6578');
INSERT INTO password(id, password) VALUES (3, 'keyword-9999');

INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (1, 1, 1);
INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (2, 2, 3);
INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (3, 3, 3);
INSERT INTO sandbox_instance_ref(id, sandbox_instance_ref, training_instance_id) VALUES (4, 4, 4);

INSERT INTO training_instance_sandbox_instance_ref(training_instance_id, sandbox_instance_ref_id) VALUES (1, 1);
INSERT INTO training_instance_sandbox_instance_ref(training_instance_id, sandbox_instance_ref_id) VALUES (2, 2);
INSERT INTO training_instance_sandbox_instance_ref(training_instance_id, sandbox_instance_ref_id) VALUES (3, 3);
INSERT INTO training_instance_sandbox_instance_ref(training_instance_id, sandbox_instance_ref_id) VALUES (4, 4);

INSERT INTO user_ref(id, user_ref_login) VALUES (1, 'Organizer1');
INSERT INTO user_ref(id, user_ref_login) VALUES (2, 'Organizer2');

INSERT INTO training_instance_organizers(training_instance_id, organizers_id) VALUES (1, 1);
INSERT INTO training_instance_organizers(training_instance_id, organizers_id) VALUES (2, 1);
INSERT INTO training_instance_organizers(training_instance_id, organizers_id) VALUES (3, 2);

INSERT INTO participant_ref(id, participant_ref_login) VALUES (1, 'Participant1');
INSERT INTO participant_ref(id, participant_ref_login) VALUES (2, 'Participant2');

INSERT INTO training_run(id, start_time, end_time, state, current_level_id, sandbox_instance_ref_id, training_instance_id, participant_ref_id, solution_taken, event_log_reference, incorrect_flag_count, assessment_responses, total_score, current_score, level_answered) VALUES (1, '2016-10-19 10:23:54+02', '2022-10-19 10:23:54+02', 'ALLOCATED', 2, 2, 2, 1, false, null, 5, '[]',30, 20, false);
INSERT INTO training_run(id, start_time, end_time, state, current_level_id, sandbox_instance_ref_id, training_instance_id, participant_ref_id, solution_taken, event_log_reference, incorrect_flag_count, assessment_responses, total_score, current_score, level_answered) VALUES (2, '2020-10-19 10:23:54+02', '2024-10-19 10:23:54+02', 'NEW', 1, 3, 3, 2, false, null, 4, '[]', 100, 10, false);
INSERT INTO training_run(id, start_time, end_time, state, current_level_id, sandbox_instance_ref_id, training_instance_id, participant_ref_id, solution_taken, event_log_reference, incorrect_flag_count, assessment_responses, total_score, current_score, level_answered) VALUES (3, '2019-10-19 10:23:54+02', '2024-10-19 10:23:54+02', 'NEW', 7, 2, 4, 1, false, null, 4, '[]', 0, 0, true);


