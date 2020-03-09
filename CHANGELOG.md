 * Merge branch '358-get-visualization-info-for-given-training-instnace-not-for-training-run' into 'master'
 * Created two endpoints to get visualization info for training instance or training run
 * Update pom.xml version based on GitLab tag. Done by CI
 * Merge branch '345-it-must-be-possible-to-force-delete-training-instance-and-training-run' into 'master'
 * Resolve "It must be possible to force delete training instance and training run"
 * Merge branch '357-change-versions-of-some-dependencies-and-resolve-build-warnings' into 'master'
 * Resolved issues with mapper and modified version of some dependencies
 * Merge branch '356-check-sandbox-in-openstack-when-delete-training-run' into 'master'
 * Resolve "Check sandbox in openstack when delete training run"
 * Merge branch '355-fix-elastic-template' into 'master'
 * Resolve "Fix elastic template"
 * Update pom.xml version based on GitLab tag. Done by CI
 * Update postinst
 * Update install
 * Update control
 * Update service
 * Update install
 * Update kypo2-training.properties
 * Update kypo2-training.properties
 * Update README.md
 * Update kypo2-training.properties
 * kypo2-training.properties added.
 * Merge branch '354-include-default-configuration-file-to-the-project-root-directory-for-debian-purposes' into 'master'
 * property file configured more properly in README.md
 * Update pom.xml version based on GitLab tag. Done by CI
 * Merge branch '353-endpoit-to-verify-flag-return-wrong-iscorrectflagdto' into 'master'
 * incorrectFlagCount of TR resets to 0 with each next level
 * Edit service user
 * Update pom.xml version based on GitLab tag. Done by CI
 * Merge branch '352-make-deb-packages-lighter' into 'master'
 * Merge branch '348-wrong-number-of-levels-in-training-run-overview' into 'master'
 * level order now updates with every level deletion
 * Do not upload Deb package as artifact
 * Do not upload Deb package as artifact
 * Install from jars, edit service file
 * Merge branch '335-add-field-solution-into-iscorrectflagdto' into 'master'
 * Added field solution to IsCorrectFlagDTO
 * Merge branch '351-modify-method-delete-sandbox-in-traininginstanceserviceimpl' into 'master'
 * Resolve "Modify method delete sandbox in TrainingInstanceServiceImpl"
 * Merge branch '349-sandbox-is-not-deleted-in-db-when-it-has-assigned-training-run' into 'master'
 * Sandboxes which are assigned to the TR can be deleted
 * Merge branch '344-add-endpoint-to-get-info-about-levels-for-specific-training-run' into 'master'
 * Added endpoint for vizualizations
 * Fix tests to the new final state.
 * Merge branch 'master' of gitlab.ics.muni.cz:kypo2/services-and-portlets/kypo2-training
 * SandboxStates added.
 * Merge branch '347-possibility-to-delete-training-run' into 'master'
 * Resolve "Possibility to delete training run"
 * Merge branch '346-fix-resume-training-run-action' into 'master'
 * It is possible to resume to the training run.
 * Add author tag.
 * Audit level title correctly.
 * Return full info about users.
 * Merge branch '343-fix-elastic-search-template-script' into 'master'
 * set-elasticsearch-template script and template updated
 * Merge branch '341-enable-https-in-spring-boot-project' into 'master'
 * Resolve "Enable HTTPS in Spring Boot project"
 * Create Deb package in deployDebian stage
 * README for generating SSL added.
 * Merge branch '340-fix-updatetraininginstance-unit-test' into 'master'
 * updateTrainingInstance service unit test fixed
 * Update pom.xml version based on GitLab tag. Done by CI
 * Merge branch '339-allow-trainee-access-training-definition-with-given-id' into 'master'
 * Trainee is allowed to get training definition with given ID
 * Repaired request JSON when create pool for sandboxes
 * Merge branch '337-automaticaly-add-logged-in-user-as-organizer-to-updated-training-instance' into 'master'
 * Logged in user is automaticaly added as organizer when update training instance
 * Merge branch '338-update-logback-spring-xml' into 'master'
 * logback updated
 * Merge branch '336-modify-handling-error-responses-from-rest-template-when-calling-pythonapi-or-userandgroup' into 'master'
 * Changed the way how to handle errors from rest template.
 * Set script for inserting an Elasticsearch template.
 * Merge branch '334-add-javadoc-to-rest-layer' into 'master'
 * Resolve "Add javadoc to rest layer"
 * Merge branch '332-integrate-logging-aspect' into 'master'
 * AOP clases integrated into project
 * Merge branch '330-add-javadoc-to-api-layer' into 'master'
 * Resolve "Add javadoc to api layer"
 * Fix typo
 * Merge branch '333-fix-wrong-error-message-in-tests' into 'master'
 * Fix expected messages in unit tests.
 * Merge branch '331-repair-and-fix-todo-issues-in-project' into 'master'
 * Cannot resume archived TR, switch TR state from one to the same one will not cause error, fixed unit test for accessTrainingRun, two different errors when finis non last or not answered level
 * Merge branch '329-impossible-to-delete-sandbox-after-failed-deletion' into 'master'
 * Resolve "Impossible to delete sandbox after failed deletion"
 * Merge branch '325-fix-and-update-integration-tests' into 'master'
 * Resolve "Fix and update integration tests"
 * Merge branch '327-modify-sandboxes-endpoint-for-full-build' into 'master'
 * query for full sandbox build fixed
 * Dont export Deb package as GitLab CI artifact
 * Merge branch '328-edit-debian-postinstall-script' into 'master'
 * Merge branch '326-add-javadoc-to-service-layer' into 'master'
 * Resolve "Add javadoc to service layer"
 * Edir debian/rules
 * Merge branch '327-modify-sandboxes-endpoint-for-full-build' into 'master'
 * Resolve "Modify sandboxes endpoint for full build"
 * Merge branch '324-fix-existsanyfortrainingdefinition-query' into 'master'
 * training instance exitsAnyForTrainingDefinition query fixed
 * Merge branch '322-accesible-training-runs-returning-incorrect-data' into 'master'
 * sorting of accesible runs by title moved from rest to facade
 * Merge branch '322-accesible-training-runs-returning-incorrect-data' into 'master'
 * +1 added to current level in accessible trainig runs DTO
 * Merge branch '320-add-javadoc-to-persistance-layer' into 'master'
 * Resolve "Add javadoc to persistance layer"
 * Merge branch '321-change-field-organizers-in-traininginstancecreatedto-and-traininginstanceupdatedto-to-organizers_login' into 'master'
 * Field in TrainingInstanceCreateDTO organizers changed to list of strings organizersLogin
 * Merge branch '319-add-field-sandboxes_with_training_run-into-traininginstancedto' into 'master'
 * Resolve "Add field sandboxes_with_training_run into TrainingInstanceDTO"
 * Merge branch '318-add-parameter-active-to-endpoint-for-obtaining-all-training-runs' into 'master'
 * Add parameter isActive into endpoint for obtaining training runs for specific training instance
 * Merge branch '317-create-endpoint-for-reallocating-sandbox-of-given-training-run' into 'master'
 * Changed states of Training run and state is changed to archived when sandbox is deleted.
 * Merge branch '315-change-createtrainingdefinitiondto-to-accept-list-of-logins-instead-of-list-of-authors' into 'master'
 * Object authors and organizers in update and create training definition DTO changed to list of logins
 * Merge branch '311-accessed-training-run-number-of-levels-attribute-has-wrong-value' into 'master'
 * added +1 to number of levels assigned to accessed runs
 * Merge branch '313-sort-on-title-of-trainee-accessed-trs-returns-error' into 'master'
 * Resolve "Sort on title of trainee accessed TRs returns error"
 * Merge branch '314-redesign-oidc-information-about-authenticated-user-to-enum-instead-of-string-to-prevent-string-mistakes' into 'master'
 * Enums instead of Strings for enumerative values provided.
 * Merge branch '312-given-and-family-name-are-null-in-user-ref-dtos' into 'master'
 * Family name and given name attribute are obtaining from user and group
 * Merge branch '310-cannot-get-ti-by-its-id' into 'master'
 * Resolve "Cannot GET TI by its id"
 * Merge branch '309-resumption-of-trainin-run-after-taking-hint-fails' into 'master'
 * Taken hints are returned, excpetion from hibernate solved
 * Merge branch '306-add-full_name-given_name-and-family_name-into-audit-events' into 'master'
 * Added full name and full name without title into audit events
 * Merge branch '305-create-class-hintinfo-for-taken-hints-in-training-run' into 'master'
 * Added TakenHintDTO into AbstractLevelDTO and remove snapshothook
 * Merge branch '291-optimize-sql-selects' into 'master'
 * Faulty unit tests fixed
 * merge with master
 * Merge branch '288-impossible-to-access-training-run-after-reallocation-of-sandbox' into 'master'
 * Problem with access training run after reallocation of sandbox solved.
 * Merge branch '287-no-error-message-when-resuming-and-sandbox-is-deleted' into 'master'
 * Exception with adequate message is thrown when resume training run without sandbox.
 * Merge branch '304-add-role-description-in-config-file-roles-json' into 'master'
 * Config file roles.json modified
 * Merge branch '303-update-erd-diagram' into 'master'
 * ERD fixed
 * Merge branch '303-update-erd-diagram' into 'master'
 * ERD updated
 * Merge branch '302-add-given-name-and-family-name-to-user' into 'master'
 * Resolve "Add given name and family name to user"
 * Merge branch '294-select-name-when-cloning-td' into 'master'
 * Merge branch '301-remove-property-file-from-deb-package' into 'master'
 * Remove property file
 * State added to TD DTO for organizers
 * added title param to clone training definition endpoint
 * Estimated duration set when updating training definition.
 * Merge branch '293-start-time-of-training-run-is-null' into 'master'
 * Resolve "Start time of training run is null"
 * Merge branch '296-source-code-inserted-into-solution-in-game-level-is-not-allowed' into 'master'
 * change JPA types.
 * Merge branch '284-add-service-and-config-files-into-deb-package' into 'master'
 * Edit service file
 * Install jars within Deb package
 * find instance by Id IT fixed after refactor
 * find training run by id optimized
 * accessTrainingRun service unit tests fixed after refactor
 * accessTrainingRun() optimized
 * training instance query selects optimized
 * TrainingDefinitionIT findLevelById tests fixed
 * training definition query selects optimized
 * findDefinitionById query optimized
 * Merge branch '292-add-boris-jadus-author-tag-to-selected-classes' into 'master'
 * Resolve "Add Boris Jadus author tag to selected classes"
 * Merge branch '289-allow-specification-of-sandbox-allocation-count' into 'master'
 * optional attribute count added to allocateSandboxes endpoint
 * Merge branch '285-add-attributes-to-accesstrainingrundto' into 'master'
 * startTime attribute added to AccessTrainingRunDTO class
 * Merge branch '290-add-instance-and-definition-id-to-trainign-run' into 'master'
 * Resolve "Add instance and definition id to trainign run"
 * Merge branch '286-change-implicit-value-of-incorrect-flag-limit' into 'master'
 * implicit incorrectFlag attribute of GameLevel changed from 0 to 100
 * Add endpoint for retrieving elasticsearch events from specific training run.
 * Merge branch '283-configure-project-to-send-logs-into-syslog-with-correct-parameters' into 'master'
 * New config of logback
 * Fix path to the config file
 * Add service file and config file to the Deb package
 * Merge branch '280-fix-deletion-of-sandboxes' into 'master'
 * Delete sandboxes repaired
 * Merge branch '279-create-one-default-question-with-new-assessment-level' into 'master'
 * Default question for assessment created
 * Merge branch '281-remove-try-again-from-possible-actions-of-training-run' into 'master'
 * TRY_AGAIN action removed from accessTrainingRunDTO class
 * Merge branch '278-add-new-state-to-training-run' into 'master'
 * Training run state ARCHIVED renamed to FINISHED
 * Merge branch '248-configure-syslog-and-logstash-to-save-events-into-database' into 'master'
 * Added configuration to logstash
 * Merge branch '276-cannot-take-hint-in-training-run-game-level' into 'master'
 * JsonProperty value fixed for hintId attribude of HintTaken event class
 * Merge branch '277-add-training-instance-id-to-trainee-endpoints' into 'master'
 * instanceId attribute added to AccessTrainingRunDTO and AccessedTrainingRunDTO classes
 * Merge branch '275-fix-transferring-token-between-asynchronous-tasks-in-spring' into 'master'
 * Resolve "Fix transferring token between asynchronous tasks in Spring"
 * Merge branch '274-sort-by-estimated-duration-does-not-change-order' into 'master'
 * Sort TD by estimated duration does change order in page
 * Merge branch '273-info-about-levels-attribute-is-empty-on-resume-training-run' into 'master'
 * Resolve "Info about levels attribute is empty on resume training run"
 * Update pom.xml version based on GitLab tag. Done by CI
 * Run CI as Kyposervice
 * Update pom.xml version based on GitLab tag. Done by CI
 * Merge branch '259-fix-import-of-training-definition' into 'master'
 * training definition import fixed
 * Update pom.xml version based on GitLab tag. Done by CI
 * Rollback to older version to force a new pipeline
 * Rollback to older version to force a new pipeline
 * Switch to parent directory after sec-commons mvn install
 * Update pom.xml version based on GitLab tag. Done by CI
 * Fix CI sed command
 * Merge branch '265-enable-cd-to-master' into 'master'
 * Dont run tests on mvn deploy
 * refactor security annotations.
 * context copying decorator added.
 * authorization token created as new string from httpservletrequest.
 * Remove twice Bearer string in authorization method.
 * pass http headers from facade layer to set token before async method.
 * Use default @Async with process executor.
 * add task executor with new security context holder in spring security.
 * Call default Async operation instead of properiatary process executor Async TaskExecutor.
 * preauthorize add to facade layer when calling allocation and deletion of sandboxes.
 * Merge branch '271-check-security-roles-run-in-new-transaction' into 'master'
 * add security checks to a new transaction.
 * Merge branch '268-fix-sorting-of-training-definitions-based-on-estimated-duration-and-last_edit-review-the-querydsl-and-pageable-options' into 'master'
 * training definition contains estimated duration attribute.
 * Merge branch '267-unpossibility-to-change-state-from-released-to-unreleased-when-the-training-instance-is-associated-to-the-particular-training-definition' into 'master'
 * update possiblity to change state from released to unreleased.
 * Merge branch '262-could-not-change-state-from-unreleased-to-released-when-training-instances-are-associated-fix' into 'master'
 * State can be changed from unreleased to released when TD have TI
 * Merge branch '264-td-swapping-is-allowed-when-update-is-not' into 'master'
 * Levels in TD cannot be swapped when TD has assigned training instance
 * interceptor configuration is autowired in service config.
 * resttemplate made as configuration class.
 * Merge branch '266-separate-service-config-class-for-copying-context-config-classes' into 'master'
 * reconfigure async operations.
 * add decorators for Async operations.
 * Edit the editVersion phase
 * Ignore .gitlab-ci.yml file during grep+sed
 * change security annotation on facade layer.
 * add security to facade layer for allocate sandboxes to avoid transfering token between threads.
 * Merge branch '261-sort-returned-abstract-levels-in-training-definition' into 'master'
 * return levels by training definition id sorted by order attribute.
 * Merge branch '260-do-not-use-testing-data-migrate-db-without-testing-data' into 'master'
 * delete testing data.
 * Merge branch '252-redesign-database-to-add-relation-between-training-definition-and-abstract-level' into 'master'
 * redesign service, facade and rest layer for including the relation between training definition and abstract level.
 * Merge branch '257-impossible-to-reallocate-after-deleting-sandbox-with-tr' into 'master'
 * Delete sandboxes which are assigned to training run
 * Merge branch '251-associated-training-definitions-returns-incorrect-can-be-edited-attribute' into 'master'
 * Can be edited attribute removed from DTO
 * Merge branch '255-missing-hints-in-game-level-of-training-run' into 'master'
 * List of hints added to GameLevelViewDTO
 * Merge branch '245-update-unit-tests' into 'master'
 * TrainingInstance service unit test for update and delete now check if start and end time are correct
 * change index name for audit actions in elasticsearch events.
 * Merge branch '246-use-methods-from-security-service-for-obtaining-sub-of-user' into 'master'
 * Methods for sub or full name of user is moved to security service
 * change divison symbol to . in the ES index.
 * elasticsearch index changed to using division symbol.
 * Merge branch '247-set-new-index-for-events-in-visualization-in-elasticsearch' into 'master'
 * elasticsearch index changed.
 * Merge branch '242-td-clone-does-not-work' into 'master'
 * In cloned training definition, beta testing group is set to null
 * Fix confict
 * Typo in .gitlab-ci fix
 * Merge branch '241-delete-sandboxes-with-pool-when-deleting-training-instance' into 'master'
 * Delete sandbox pool when training instance is deleted
 * Merge branch '243-assessment-level-has-incorrect-value-of-estimated-duration' into 'master'
 * Estimated duration of new assessment level set to 1
 * Add info texts to CI
 * Change pom.xml version deps recursively
 * Merge branch '219-add-audit-events-to-the-right-places' into 'master'
 * Resolve "Add audit events to the right places"
 * Merge branch '232-deleting-td-with-associated-ti-throws-sql-constraint-error' into 'master'
 * Attempt to remove definition with created instance now throws meaningful error message
 * Merge branch '240-update-td-in-ti-not-saved-in-db' into 'master'
 * TI updates properly
 * Merge branch '225-change-implicit-value-of-incorrect-flag-limit' into 'master'
 * Resolve "Change implicit value of incorrect flag limit"
 * Merge branch '235-sandbox-allocation-does-not-work-properly' into 'master'
 * Allocate sandboxes reapired, possible to allocate sandboxes which left in pool
 * Merge branch '236-filter-training-instances-user-can-see' into 'master'
 * Find all training instances returns only training instances of logged in organizer
 * Merge branch '231-create-endpoint-getalltdfordesigners-and-getalltdfororganizers' into 'master'
 * Resolve "Create endpoint GetAllTDForDesigners and GetAllTDForOrganizers"
 * Merge branch '234-training-definition-call-does-not-return-levels' into 'master'
 * levels removed from find all training definitions.
 * add surefire plugin to all modules.
 * surefire plugin ignore tests.
 * Merge branch '233-downgrade-the-version-of-surefire-plugin-to-2-22-0-to-pass-the-tests' into 'master'
 * upgrade the surefire plugin.
 * Merge branch '226-add-estimated-duration-column-to-designer-overview' into 'master'
 * Resolve "Add estimated duration column to designer overview"
 * Merge branch '222-refactor-and-investigate-the-resttemplate-to-include-the-token-for-sending-the-authenticated-user' into 'master'
 * Token is automatically added to header when using rest template
 * Merge branch '227-add-last-edit-collumn-to-designer-overview' into 'master'
 * Resolve "Add last edit collumn to designer overview"
 * Merge branch '224-openstack-400-bad-request-erro-when-allocate-sandboxes' into 'master'
 * Resolve "OpenStack:400 Bad Request erro when allocate sandboxes"
 * Merge branch '229-fix-export-td-and-ti-http-406-error' into 'master'
 * rework exporting files to do it in memory not with temp file.
 * Merge branch '228-make-allocation-sandboxes-as-async-call' into 'master'
 * allocate and reallocate of sandboxes are asynchronous operations now
 * Merge branch '220-refactor-endpoints-for-export-to-design-them-not-to-return-json-dto-but-json-file-which-is-not-serialized-to-one-line' into 'master'
 * Resolve "Refactor endpoints for export to design them not to return json dto but json file which is not serialized to one line"
 * Merge branch '223-authorize-annotations-in-training-instance-service-are-wrong' into 'master'
 * Repaired preauthorize annotations in training instance service
 * Merge branch '218-change-returned-size-of-documents-from-elasticsearch' into 'master'
 * index number of returned documents the limit set to 10000.
 * Merge branch '217-fix-script-to-upload-event-data-to-the-elasticsearch' into 'master'
 * script to insert documents to the Elasticsearch added.
 * Merge branch '215-create-new-folder-for-events-and-split-events-to-them-modify-script-to-send-info-to-elastic-db' into 'master'
 * Resolve "Create new folder for events and split events to them, modify script to send info to elastic DB"
 * Merge branch '216-training-definition-find-by-id-return-the-list-of-levels-in-the-their-game-order' into 'master'
 * Change the list of authors from Set to List collection.
 * Merge branch '206-add-another-parameter-for-visualizations-about-game-time-from-the-beginning' into 'master'
 * Resolve "Add another parameter for visualizations about game time from the beginning"
 * Merge branch '173-debianize-this-project' into 'master'
 * Resolve "Debianize this project"
 * Merge branch '214-sort-events-by-timestamp-attribute' into 'master'
 * get trainings events sorted by timestamp attribute.
 * Dont install sec-comms in Debian Docker image
 * Install security-commons as a pre-task
 * Edit rules file
 * Merge branch '212-new-index-for-elasticsearch-events' into 'master'
 * Match all documents under parametrized index.
 * Merge branch '211-change-format-of-responses-to-emi-questions' into 'master'
 * Format of responses to EMI questions changed
 * Merge branch '210-td-prefix-uploaded' into 'master'
 * Issue with set title and null pointer exception in test resolved.
 * set prefix for uploaded training definitions.
 * Uploaded prefix for uploaded training definitions.
 * Merge branch '209-newly-imported-training-definition-should-have-state-unreleased-by-default' into 'master'
 * Resolve "Newly imported Training Definition should have state unreleased by default"
 * Merge branch '201-cannot-get-all-training-definitions-when-user-have-role-designer-and-organizer' into 'master'
 * Merge branch 'master' into 201-cannot-get-all-training-definitions-when-user-have-role-designer-and-organizer
 * Merge branch '208-released-td-should-be-editable-if-it-has-no-ti-created' into 'master'
 * Resolve "Released TD should be editable if it has no TI created"
 * Debianize the project + enable CI
 * Merge branch '205-resolve-non-saving-hints' into 'master'
 * Hints are being saved properly
 * Merge branch '207-remove-restraints-in-clone-method' into 'master'
 * unreleased TD can be cloned now
 * Merge branch '204-add-event-trainingrunsurrendered' into 'master'
 * TrainingRunSurrendered even added.
 * Repair failing tests.
 * Merge branch '202-rename-rest-endpoint-for-exporting-training-instances' into 'master'
 * rename rest endpoint for exporting training instances.
 * Merge branch '199-archive-download-of-the-all-training-instance-data-a-user-can-archive-only-training-instances-which-passed-the-end-time' into 'master'
 * service unit tests added for archiveTrainingInstance method
 * facade unit tests added for archiveTrainingInstance method
 * controller unit tests added for ArchiveTrainingInstance method
 * archiveTrainingInstance now throws conflict exception correctly
 * only finished instances can be archived
 * Training Runs added to archiveTrainingInstance export
 * archiveTrainingInstance() archives instance, definition and levels
 * archive training instance endpoint created
 * New DTO classes created for training instance archivation
 * Merge branch '200-reallocate-sandboxes-should-not-allocate-whole-pool' into 'master'
 * reallocate sandbox method now allocates only one sandbox
 * Merge branch '198-find-all-training-definitions-return-only-logged-in-author' into 'master'
 * Training definition repository modified, instead join fetch use inner join
 * Merge branch '197-return-all-events-in-training-definition-in-specific-training-instance' into 'master'
 * Search events by TD id and TI id.
 * Merge branch '196-wrong-query-in-elasticsearch-testing-data-curl-es-6-7-sh-file' into 'master'
 * elasticsearch query for inserting template repair with JSON date format
 * Merge branch '195-emi-questions-in-assessment-can-be-asymetric' into 'master'
 * Modified json schema and emi questions
 * Merge branch '193-implementing-elasticsearch-queries-into-java' into 'master'
 * Resolve "implementing elasticsearch queries into java"
 * Merge branch '174-rework-allocatesandboxes-method' into 'master'
 * Resolve "Rework allocateSandboxes method"
 * Required for id queries on rest added.
 * Merge branch '194-change-permission-for-retrieving-associated-training-definitions-to-sandboxes' into 'master'
 * permission for retrieving training definitions by sandbox definitions changed from admin ro designer or admin.
 * Merge branch '190-set-custom-loggin-in-project' into 'master'
 * Resolve "Set custom loggin in project"
 * Merge branch '192-update-training-definition-save-null-user-in-beta-testing-group' into 'master'
 * Null user in beta testing group when update training definition resolved.
 * Merge branch '191-change-name-of-entity-view-group-to-betatesters-and-change-it-to-optional' into 'master'
 * Resolve "Change name of entity View Group to BetaTesters and change it to optional"
 * Merge branch '189-new-version-of-flyway-5-2-4' into 'master'
 * Changed versions of flyway from 5.0.7 to 5.2.4
 * Merge branch '188-get-solution-taken-for-the-second-time-is-also-audited' into 'master'
 * Resolve "Get solution taken for the second time is also audited"
 * Merge branch '181-user-without-admin-rules-cannot-download-training-definition' into 'master'
 * All designers can download training definition
 * Merge branch '175-admin-role-is-needed-to-see-training-instances' into 'master'
 * Organizers are allowed to see all training instances
 * Merge branch '177-state-of-training-run-is-not-stored-properly' into 'master'
 * Solution taken twice decrease total score only once
 * Merge branch '187-resume-training-if-keyword-inserted-twice' into 'master'
 * Accessed training run return correct level order/number of levels. Check training runs of participnat when access some training run.
 * Edit readme flyway command.
 * Merge branch '169-adjust-training-to-new-way-of-managing-roles-in-security-commons-and-user-and-group' into 'master'
 * Resolve "Adjust training to new way of managing roles in security commons and user and group"
 * Merge branch '168-implementation-of-trainingrun-integration-tests' into 'master'
 * Resolve "implementation of trainingRun integration tests"
 * Merge branch '186-authors-are-added-to-original-td-in-clone-method-original-authors-are-cloned-into-copy' into 'master'
 * problem with authors while cloning fixed
 * Merge branch '184-add-pool-id-to-traininginstancedto' into 'master'
 * PoolId added to training instance dto