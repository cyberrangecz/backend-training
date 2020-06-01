* 7698e0d1 -- Update pom.xml version based on GitLab tag. Done by CI.
* 6c7c7700 -- Update VERSION.txt
*   c3e3e64f -- Merge branch '550-repair-configuration-of-webclient-for-sandbox-service' into 'master'
|\  
| * a3af62f1 -- Exchange filter function moved to web client for sandbox service
* | bf7f1944 -- Update pom.xml
|/  
*   1ed754d2 -- Merge branch '546-check-the-code-and-remove-dev-profile-if-not-necessary' into 'master'
|\  
| * 15b81f65 -- Removed profiles DEV, PROD and all related stuff
* | db43eaeb -- CHANGELOG.md file updated with commits between the current and previous tag. Done by CI.
* | 9285c60e -- Update pom.xml version based on GitLab tag. Done by CI.
* | 30a3dff9 -- Update VERSION.txt
* |   8cf4feda -- Merge branch '549-fix-archive-training-instance' into 'master'
|\ \  
| * | 04c764f6 -- archive TI fixed
|/ /  
* | 68765a14 -- CHANGELOG.md file updated with commits between the current and previous tag. Done by CI.
* | b828d8f5 -- Update pom.xml version based on GitLab tag. Done by CI.
* | 45df7432 -- rest high level client to retrieve elasticsearch document was configured VERSION.txt
* | fb7ae1a2 -- configure elasticsearch resthighlevel client.
* | cf447a7e -- remove unused logging properties.
* |   a03de6a3 -- Merge branch 'master' of gitlab.ics.muni.cz:kypo-crp/backend-java/kypo2-training
|\ \  
| * | 8b6e830a -- CHANGELOG.md file updated with commits between the current and previous tag. Done by CI.
| * | af209ada -- Update pom.xml version based on GitLab tag. Done by CI.
| |/  
| * d515f2fb -- Update VERSION.txt
* | 9e740c3b --  remove web client from elasticsearch module.
|/  
* 679b2f15 -- remove dot before wildcard.
*   c9f6b696 -- Merge branch '548-change-elasticsearch-index' into 'master'
|\  
| * 689dfead -- change elasticsearch index .
|/  
* a223c610 -- CHANGELOG.md file updated with commits between the current and previous tag. Done by CI.
* e1c41c71 -- Update pom.xml version based on GitLab tag. Done by CI.
* 4153583a -- Update VERSION.txt
* 74bd63b0 -- Update logback-spring.xml
* 3a570a54 -- Update logback-spring.xml
* 2d5d5582 -- Update kypo2-training-dev.properties
* ce355791 -- Update kypo2-training-prod.properties
* d78c22cd -- Update kypo2-training-prod.properties
* 0e641b98 -- Update training.properties
*   46f953fa -- Merge branch '547-change-elasticsearch-index-for-documents-retriaval' into 'master'
|\  
| * a666a468 -- change elasticsearch index
|/  
* 17cd24bb -- Update AbstractKypoIndexPath.java to kypo from kypo3
*   3822284f -- Merge branch '545-create-dto-to-validate-flag-instead-of-raw-string' into 'master'
|\  
| * 10df7c89 -- Created ValidateFlagDTO
|/  
*   758950ac -- Merge branch '541-validate-flag-content-to-avoid-bugs-regarding-the-solution-flag-that-is-composed-to-symbols' into 'master'
|\  
| * 462579ce -- IsCorrectFlag endpoint - param flag changed to request body
|/  
*   4aca7cec -- Merge branch '544-rename-rsyslog-to-syslog' into 'master'
|\  
| * ac0b4c6c -- Resolve "RENAME RSYSLOG to SYSLOG"
|/  
*   fde81fdf -- Merge branch '505-check-swagger-messages-and-documentation-in-general' into 'master'
|\  
| * 8e8572ef -- Resolve "Check Swagger messages and documentation in general"
|/  
* 834a6afc -- CHANGELOG.md file updated with commits between the current and previous tag. Done by CI.
* da8be0c8 -- Update pom.xml version based on GitLab tag. Done by CI.
* 44de9ed9 -- Update VERSION.txt
*   37e31298 -- Merge branch '542-add-syslogappender-for-audit-logs-in-logback-xml' into 'master'
|\  
| * 56028e17 -- Add Syslog Appender (missing UTF-8 setting, missing the possibility to add host and port from external configuration)
|/  
*   27ea38f9 -- Merge branch '543-edit-dockerfiles-so-only-essential-files-are-copied' into 'master'
|\  
| * e51d98c9 -- Resolve "Edit Dockerfiles so only essential files are copied"
|/  
* 47a3bb25 -- Update Security commons version to 1.0.32
* 46ef1f2e -- CHANGELOG.md file updated with commits between the current and previous tag. Done by CI.
* ee16240e -- Update pom.xml version based on GitLab tag. Done by CI.
*   a82b3148 -- Merge branch '540-use-v-prefix-for-tags' into 'master'
|\  
| * 2a4effba -- Resolve "Use 'v' prefix for tags"
|/  
* e6d1a251 -- Update kypo2-training-prod.properties
* 6b2d4777 -- Update kypo2-training-dev.properties
* 9c0d4f1c -- CHANGELOG.md file updated with commits between the current and previous tag. Done by CI.
* 2d809cf6 -- Update pom.xml version based on GitLab tag. Done by CI.
* 43212206 -- security commons version updated.
* ef106768 -- Update security-commons version.
* 8790986f -- add logback-spring.xml
* bff43656 -- Update supervisord.conf
*   d99dbde8 -- Merge branch '539-replace-dpath-to-config-file-by-spring-config-location' into 'master'
|\  
| * 46f27597 -- Path.to.config.file replace by spring.config.location and added example of set logging in example properties
|/  
*   1978b4db -- Merge branch '538-rename-service-name-to-microservice-name-in-kypo2-training-properties' into 'master'
|\  
| * 41d474ae -- Resolve "Rename service.name to microservice.name in kypo2-training-dev.properties"
|/  
*   c27eb5fc -- Merge branch '535-force-delete-training-instances-fails' into 'master'
|\  
| * ca8b3ac5 -- Repair delete of training instance
|/  
*   bfd8e543 -- Merge branch '534-sandbox-id-should-be-removed-from-archived-training-run' into 'master'
|\  
| * 69a4278f -- Sandbox id removed from archived run
|/  
*   3cfeac46 -- Merge branch '531-add-endpoint-to-archive-training-run' into 'master'
|\  
| * b03ce480 -- Resolve "Add endpoint to archive training run"
|/  
*   b71a1c99 -- Merge branch '533-replace-checking-of-definition-by-common-method-when-create-levels' into 'master'
|\  
| * 5a789fa0 -- Checking of Training definition replaced by method
|/  
*   1e7be9b7 -- Merge branch '532-update-trainingdefinition-integration-tests' into 'master'
|\  
| * da4d6620 -- Tests updated
* | 12cc6530 -- Update supervisord.conf
|/  
* 35d6e9d9 -- Update supervisord.conf
* dbb62b59 -- Update supervisord.conf
* dbdfc0a0 -- Update supervisord.conf
* c773aa7b -- Update supervisord.conf with autorestart postgresql to 5 times
* a3b0d9de -- update Dockerfile supervisor d.
* 57dd65eb -- security-commons version upgraded.
* 4e5ad994 -- Update Dockerfile
*   96bbf134 -- Merge branch '530-update-training-definition-rest-unit-tests' into 'master'
|\  
| * d5ef0225 -- TrainingDefinitionRestController tests updated
|/  
*   af48a935 -- Merge branch '529-update-training-instance-rest-unit-tests' into 'master'
|\  
| * 8b6f68b3 -- Resolve "Update Training Instance rest unit tests"
|/  
*   7ff60f90 -- Merge branch '528-update-training-run-rest-unit-tests' into 'master'
|\  
| * 922044e8 -- Resolve "Update Training Run rest unit tests"
|/  
*   f61c6c0f -- Merge branch '527-move-the-important-error-message-for-user-to-attribute-message-in-apierror' into 'master'
|\  
| * 9926f24e -- Message for user moved to api error message
|/  
*   3156953b -- Merge branch '524-repair-tests-after-fix-access-training-run-transactions' into 'master'
|\  
| * 296ceeec -- Repaired test after fix of access training run
|/  
* 56d7036e -- When any exception is thrown acquisition lock is deleted.
*   df3afdf1 -- Merge branch '523-fix-access-training-run-transactions' into 'master'
|\  
| * cbe3def5 -- Resolve "Fix access training run transactions"
|/  
* 6f8df5fd -- WebCLient body wrapped to Mono.just.
* 5bb55f11 -- remove deprecated BodyInserts.fromObject method.
* 2e528118 -- Update Dockerfile
*   392502ba -- Merge branch '522-update-training-run-facade-unit-tests' into 'master'
|\  
| * ce80af63 -- TrainingRun facade unit tests updated
|/  
*   15a69690 -- Merge branch '521-update-training-instance-facade-unit-tests' into 'master'
|\  
| * 85a72d49 -- TrainingInstance facade unit tests updated
* |   f0923c6d -- Merge branch '520-update-training-definition-facade-unit-tests' into 'master'
|\ \  
| |/  
|/|   
| * 43dfe1f3 -- TrainingDefiniiton facade unit tests updated
|/  
*   b42f7fba -- Merge branch '518-update-training-run-unit-tests' into 'master'
|\  
| * 9e8ed60d -- TrainingRunService tests completed
* |   32dcc227 -- Merge branch '517-update-training-instance-unit-tests' into 'master'
|\ \  
| |/  
|/|   
| * 2f2cb4c6 -- Completed tests for TraininInstanceService
* |   595eda15 -- Merge branch '516-update-training-definition-unit-tests' into 'master'
|\ \  
| |/  
|/|   
| * d2e91571 -- trainingDefinitionService unit tests updated
|/  
*   7cbc3955 -- Merge branch '513-replaced-deprecated-resttemplate-with-newer-webclient' into 'master'
|\  
| * b98e3acc -- Resolve "Replaced deprecated restTemplate with newer WebClient"
|/  
*   67ca8759 -- Merge branch '515-validation-of-dtos-for-import-and-update' into 'master'
|\  
| * f16051b0 -- Added validation for import and update levels
|/  
* 96b4573c -- Update TrainingInstanceFacade.java
*   17b007cd -- Merge branch '514-fix-whitespace-problem-with-createassessmentlevel-integration-test' into 'master'
|\  
| * 29b0a0bb -- faulty assert removed from createAL test
|/  
*   f2245521 -- Merge branch '507-rename-object-fields-in-objects-from-openstack-sandbox-api' into 'master'
|\  
| * 6e5e7d0d -- Resolve "Rename object fields in objects from OpenStack Sandbox API"
|/  
*   cab30756 -- Merge branch '495-refactor-resttemplate-call-for-null-objects-checks-and-rename-exceptions-from-external-services' into 'master'
|\  
| * 749e5ae2 -- Resolve "Refactor restTemplate call for null objects checks and rename exceptions from external services"
|/  
* 029074ee -- format constructor arguments.
*   a15175c4 -- Merge branch '497-refactor-createassessmentlevel' into 'master'
|\  
| * fdd7b19c -- Question set by creating new AssessmentQuestion which is mapped to string
* | a7cd4ccf -- add parentheses.
* | ffdc54d4 -- getUserRefDTO pass uri variable without string concat.
* | 867ab812 -- remove casting in map method in modelmapper.
* |   b93a2756 -- Merge branch '512-update-security-commons-verison' into 'master'
|\ \  
| * | acb8ce5b -- security-commons version updated.
|/ /  
* | 810acecd -- Update .gitlab-ci.yml
|/  
*   096cd10f -- Merge branch '500-refactor-evaluateresponsestoassessment' into 'master'
|\  
| * c89a1cb3 -- Resolve "Refactor evaluateResponsestoAssessment"
|/  
*   c5691d13 -- Merge branch '511-rename-login-to-sub-in-userrefdtos-calling-user-and-group-object' into 'master'
|\  
| * e12306fc -- Changed login to sub in UserRefDTO
* |   33fd4a20 -- Merge branch '498-refactor-clonelevelsfromtrainingdefinition' into 'master'
|\ \  
| * | e363e453 -- Resolve "refactor cloneLevelsFromTrainingDefinition"
|/ /  
* |   f8dce8c4 -- Merge branch '503-trainingrunfacade-refactor-deleteinfoaboutcorrectnessfromquestions' into 'master'
|\ \  
| |/  
|/|   
| * efa9bbbf -- Method deleteInfoAboutCorrectnes simpliefied and added comments
* |   fb87a1df -- Merge branch '506-remove-trailing-slashes-in-rest-api' into 'master'
|\ \  
| * | 469d212b -- refactor trailing slashes for python calls.
| |/  
* |   730999ed -- Merge branch '510-create-validator-for-responses-to-assessments' into 'master'
|\ \  
| |/  
|/|   
| * 210b0ecb -- Resolve "Create validator for responses to assessments"
|/  
*   14b5b8cf -- Merge branch '502-refactor-trainingrunfacade-converttoaccessedrundto' into 'master'
|\  
| * 874297c0 -- convertToAccessedRunDto refactored
|/  
*   f8ca94ed -- Merge branch '494-simplify-methods-in-userservice' into 'master'
|\  
| * 61d3ace8 -- Created private method to setCommonsParams of UriBuilder
* |   40cb00a6 -- Merge branch '496-refactor-creation-of-new-initial-levels' into 'master'
|\ \  
| * | f3708d2e -- new level creation refactored
| |/  
* |   d31b13cf -- Merge branch '499-refactor-update-method-in-traininginstanceservice' into 'master'
|\ \  
| * | f68a0275 -- training instance update refactored
* | |   5b75a07c -- Merge branch '509-move-converttotakenhintdto-method-to-hintmapper' into 'master'
|\ \ \  
| |_|/  
|/| |   
| * | 597c2008 -- Method convertToTakenHintDTO moved to HintMapper
* | |   4da414d0 -- Merge branch '508-create-method-to-find-abstract-level-in-trainingdefinitionservice' into 'master'
|\ \ \  
| |_|/  
|/| |   
| * | 091c2329 -- Added private method to findAbstractLevelByIdWithoutDefinition
|/ /  
* |   3c3062bc -- Merge branch '492-change-resttemplate-calls-to-formated-string-instead-of-string-concat' into 'master'
|\ \  
| |/  
|/|   
| * ae5a079e -- Resolve "Change RestTemplate calls to formated string instead of string concat"
|/  
*   dbaa5d4d -- Merge branch '504-refactor-trainingdefinitionsrestcontroller-input-state-from-string-to-existing-enum' into 'master'
|\  
| * b5700858 -- State in endpoint findAllTrainingDefinitionsForOrganizers changed to enum
* |   0d993ac8 -- Merge branch '501-trainingrunfacade-delete-or-modify-createpagination' into 'master'
|\ \  
| |/  
|/|   
| * fc30f895 -- Resolve "TrainingRunFacade delete or modify createPagination"
|/  
* 30635dde -- refactor local variable name from uriParameters to uriVariables.
* 04a12a63 -- add parentheses.
*   05355593 -- Merge branch 'master' of gitlab.ics.muni.cz:kypo-crp/backend-java/kypo2-training
|\  
| *   93f79597 -- Merge branch '493-use-sslcontextbuilder-instead-of-static-method-getinstance-in-rest-template-configuration' into 'master'
| |\  
| | * 0495e1ff -- Static method SSLContext.getInstance changed for SSLContextBuilder
| |/  
* | 8d080e07 -- refactor findTrainingRunsBzTrainingInstance.
* | 43e23ef6 -- RestTemplate reworked to compose url calls with format.
|/  
* 976688c2 -- set constructor for ELK events private.
*   72515b2f -- Merge branch '491-remove-unused-convertjsonbytestoobject-methods' into 'master'
|\  
| * 021c3d14 -- unused methods removed.
|/  
* 8b8feb9d -- remove unused parameters in rest controllers.
* 23e1c2e2 -- reformat rest controllers for visualizations.
* 42cd9e64 -- Reformat code.
*   d36f7c93 -- Merge branch '490-merge-level-mappers' into 'master'
|\  
| * ddaae54c -- Resolve "Merge level mappers"
|/  
*   1c41280d -- Merge branch '489-remove-configuration-of-trust-store-for-rest-template' into 'master'
|\  
| * 95648eaa -- Removed SSLContext configuration
|/  
* a4114a53 -- remove aop package and sandbox states class.
*   982e3686 -- Merge branch '488-uricomponentsbuilder-change-from-tostring-touristring-method' into 'master'
|\  
| * 1ec397ad -- change toString methods in restCalls to toUriString
|/  
*   f76b253d -- Merge branch '487-refactor-pathvariable-names-to-make-it-more-consistent-throughout-the-api' into 'master'
|\  
| * e399223f -- make consistent API REST resources in path variable names.
|/  
*   eae083af -- Merge branch '485-move-security-checks-inside-methods-from-service-to-facade-layer' into 'master'
|\  
| * 71ec0e0e -- Resolve "Move security checks inside methods from service to facade layer and refactor getEntity in RestTemplate for getForObject"
|/  
*   7b0f56dd -- Merge branch '486-fix-bug-with-with-non-null-checking-for-lock-id-on-pool' into 'master'
|\  
| * 9cc1b767 -- Resolve "Fix bug with with non null checking for lock id on pool on unassign action"
|/  
*   7b8a8039 -- Merge branch '456-refactor-rest-templates' into 'master'
|\  
| * 4f06a208 -- Resolve "Refactor rest templates"
|/  
*   90b3e51a -- Merge branch '484-fix-failing-unit-test' into 'master'
|\  
| * 62eeac16 -- failing unit test fixed
|/  
*   1b7ed255 -- Merge branch '483-create-custom-method-to-get-all-training-instances-of-logged-in-user' into 'master'
|\  
| * ce1b77f8 -- Query predicate replaced by JPQLQuery
* |   7288b6f2 -- Merge branch '465-remove-assert-nonnull-where-it-is-not-necessary' into 'master'
|\ \  
| |/  
|/|   
| * 74596282 -- Resolve "Remove assert.nonNull where it is not necessary"
|/  
* 211e164e -- Provide full info in exception handling for delete training instance.
* 9b4bb1b3 -- Use correct constructor for initializing EntityErrorDetail.
*   a180c026 -- Merge branch '482-retrieve-information-from-django-openstack-api-about-sandbox-definition-when-exporting-training' into 'master'
|\  
| * 9ba0ad46 -- Resolve "Retrieve information from Django Openstack API about sandbox definition when exporting training instances"
|/  
*   51934c38 -- Merge branch '481-dissable-logging-to-console-in-integration-tests' into 'master'
|\  
| * f62442a2 -- logging disabled in IT
* |   1f43d302 -- Merge branch '480-remove-pool-size-attribute-from-training-instance' into 'master'
|\ \  
| |/  
|/|   
| * e88c1bc0 -- Resolve "Remove pool size attribute from training instance"
|/  
*   b049f654 -- Merge branch '479-fix-assigntraining-concurrent-access-bug' into 'master'
|\  
| * 4435f5bd -- Resolve "Fix assignTraining concurrent access bug"
|/  
* 870ff92a -- CHANGELOG.md file updated with commits between the current and previous tag. Done by CI.
* b42eae5d -- Update pom.xml version based on GitLab tag. Done by CI.
* 626c43a8 -- Update VERSION.txt
*   da562c35 -- Merge branch '477-add-javadocs-for-repository-service-and-facade-methods' into 'master'
|\  
| * 66d6f7a8 -- Resolve "Add JavaDocs for Repository Service and Facade methods"
|/  
* 9df3365d -- Update Dockerfile
*   76d4b6f4 -- Merge branch '476-create-file-extensions-abstract-class-for-contants' into 'master'
|\  
| * 0ed59760 -- Use Abstract file extensions.
|/  
*   d5d4083a -- Merge branch '475-refactor-isadmin-isdesigner-and-isorganizer-into-one-generic-method' into 'master'
|\  
| * d98e3c12 -- Refactor securityService isAdmin, isOrganizer etc methods to one generic method.
|/  
*   b672e40d -- Merge branch '469-fix-changelog-generation-in-gitlab-ci' into 'master'
|\  
| * c3f32c0e -- Resolve "Fix changelog generation in Gitlab CI"
|/  
*   0a52c74b -- Merge branch '474-refactor-method-for-exporting-information-about-particular-training-instance' into 'master'
|\  
| * c786abca -- refactor archive training instance method.
|/  
*   5ec02bac -- Merge branch '468-refactor-or-format-spring-data-queries' into 'master'
|\  
| * 34311328 -- Resolve "Refactor or format Spring Data queries"
|/  
* 04a0a708 -- Update .gitlab-ci.yml
*   eab847af -- Merge branch '472-remove-settings-xml-from-etc-folder' into 'master'
|\  
| * f2a3ccf5 -- remove settings.xml.
|/  
*   d4626494 -- Merge branch '470-delete-unused-methods' into 'master'
|\  
| * 02e3126b -- add remove unused methods.
* | 9c7ecdbb -- unused methods removed.
|/  
*   adaf2510 -- Merge branch '464-delete-training-instances' into 'master'
|\  
| * 4257725c -- Resolve "Delete training instances"
|/  
*   159dcf4f -- Merge branch '463-deletion-of-training-run-based-on-the-training-run-state' into 'master'
|\  
| * c5a2efd8 -- downgrade security-commons version.
| * 998fadc0 -- upgrade security-commons version.
| * bfcd8a5c -- force delete parameter set to false.
| * 0d0d146c -- Delete training run refactored with the possibility of force delete.
* |   b6b368e6 -- Merge branch '462-implement-assign-and-unassign-poolid-rest-resources-for-training-instances' into 'master'
|\ \  
| * | c0d341d7 -- REST resources and service layer for assign and unassign implemented. Still need to fix thrown Exception.
|/ /  
* |   5b1a81d0 -- Merge branch '463-deletion-of-training-run-based-on-the-training-run-state' into 'master'
|\ \  
| |/  
|/|   
| * 9ff46a8a -- Resolve "Deletion of Training run based on the training run state"
|/  
*   9f401c3a -- Merge branch '466-remove-debian-package-in-this-project' into 'master'
|\  
| * 19222838 -- Debian package removed.
|/  
*   51267d8e -- Merge branch '453-prepare-test-data-factory' into 'master'
|\  
| * 0d2673a8 -- Resolve "Prepare test data factory"
|/  
*   612eef34 -- Merge branch '461-fix-gamelevel-mapper' into 'master'
|\  
| * 64dd27b2 -- AttachmentMapper added to spring boot test classes
|/  
* 8e905076 -- Update .gitlab-ci.yml
* 3262161d -- Update .gitlab-ci.yml
* a56934d1 -- Update .gitlab-ci.yml
* 2841f440 -- Update .gitlab-ci.yml
*   ef0ef8a3 -- Merge branch '429-add-attachment-field-into-gamelevelimportdto' into 'master'
|\  
| * 99469712 -- Resolve "Add attachment field into GameLevelImportDTO"
|/  
*   b1c0a498 -- Merge branch '460-add-lombok-for-builder-pattern-to-audit-classes' into 'master'
|\  
| * 4100a124 -- Lombok added.
|/  
*   e0f66474 -- Merge branch '458-refactor-delete-training-run-methods' into 'master'
|\  
| * 31707f18 -- Delete Trainin Run adjusted to new requirements
|/  
*   bffcdd36 -- Merge branch '446-update-docker-with-supervisord' into 'master'
|\  
| * 6b40d7f5 -- Resolve "Update docker with supervisord"
|/  
* cd23b876 -- CHANGELOG.md file updated with commits between the current and previous tag. Done by CI.
* d2571e1a -- Update pom.xml version based on GitLab tag. Done by CI.
* 7194dd8d -- Test Gitlab CI
*   bc1c4d36 -- Merge branch '457-remove-interfaces-for-serviceimpl-methods' into 'master'
|\  
| * 17ccb5d1 -- Resolve "Remove Interfaces for ServiceImpl methods."
|/  
*   22bf7588 -- Merge branch '455-refactor-sandboxes-management' into 'master'
|\  
| * a7adfb18 -- Resolve "Refactor sandboxes management"
|/  
* ec1798bb -- Update .gitlab-ci.yml
* edf106aa -- Update .gitlab-ci.yml
* 6064f9b0 -- test Gitlab CI.
* e9afb876 -- Update version to test Gitlab CI.
* d2fe6614 -- Add Changelog.md.
* 0781920b -- Update .gitlab-ci.yml
* 96349a2f -- Gitlab CI deploy keys added.
* af78de25 -- Add new version.
* d43e1c07 -- add VERSION.txt file.
* 7a340161 -- Update gitlab-ci.yml based on the Gitlab CI configuration from UaG
*   6d5e02d8 -- Merge branch '454-refactor-elasticsearch-events-with-common-super-class-for-ids' into 'master'
|\  
| * 2eb1cd84 -- Add poolId to events.
|/  
*   3f27f027 -- Merge branch '452-refactor-exceptions-in-kypo-training' into 'master'
|\  
| * 73c5666a -- Resolve "Refactor exceptions in KYPO training"
|/  
*   d46423a7 -- Merge branch '451-move-security-annotations-from-service-layer-to-facade-layer' into 'master'
|\  
| * 7e41288f -- Move security annotations to the facade layer.
|/  
*   5e97dc10 -- Merge branch '450-clean-pom-xml-files-move-versions-to-dependencymanagement' into 'master'
|\  
| * 7ce93ac4 -- clean pom.xml files.
|/  
*   df50cbe4 -- Merge branch '449-remove-aop' into 'master'
|\  
| * 3c4a33fb -- AOP removed.
|/  
* ffee8b62 -- Delete Vagrantfile
* fdd3290a -- Remove empty try-catch block
* 2154f0a6 -- Update pom.xml version based on GitLab tag. Done by CI
*   49f10d1d -- merged.
|\  
| * 4b7f758e -- Update copyright
| * eebb4593 -- Update copyright
* | e09b4fb7 -- Upgrade security-commons version and remove developers from maven.
|/  
* 5b4b6d10 -- remove unused folders.
* 4b4ef7f6 -- Update README.md
* 853f9926 -- Add LICENSE
*   c6288041 -- Merge branch '445-remove-author-tags' into 'master'
|\  
| * 133b9b90 -- Resolve "Remove author tags"
|/  
* 02bb0713 -- Update pom.xml version based on GitLab tag. Done by CI
*   77d9a093 -- Merge branch '444-fix-failing-test-in-traininginstanceservicetest-class' into 'master'
|\  
| * e7017f48 -- test fixed
|/  
*   0414ae1d -- Merge branch '443-authors-are-erased-when-admin-updates-definition' into 'master'
|\  
| * 0d048a89 -- Resolve "Authors are erased when admin updates definition"
|/  
*   9046145e -- Merge branch '442-two-same-hints-in-game-level-are-not-stored-in-db' into 'master'
|\  
| * 87df5320 -- Added order attribute to equals and hashCode in Hint entity
|/  
*   278bd50b -- Merge branch '441-handle-process-of-delete-training-instance-in-case-of-pool-is-deleted-in-openstack' into 'master'
|\  
| * 4ab72cd8 -- Resolve "Handle process of delete training instance in case of pool is deleted in Openstack"
|/  
*   090c5886 -- Merge branch '439-handle-process-delete-training-run-in-case-of-sandbox-instance-is-deleted-in-openstack' into 'master'
|\  
| * a52e47b0 -- Handling proces of deleting TR when sandbox is not in Python API
|/  
* 4901ebf5 -- Update pom.xml version based on GitLab tag. Done by CI
*   32318f9a -- Merge branch '440-remove-all-comments' into 'master'
|\  
| * 4326f5d6 -- Resolve "Remove all comments"
|/  
* 32dece51 -- get rid of calling python api for find all training instances.
* 51bcd5e9 -- Update pom.xml version based on GitLab tag. Done by CI
* ef2ea340 -- Fix error when deleting sandboxes.
*   bb43cdd3 -- Merge branch '438-fix-faulty-facade-tests' into 'master'
|\  
| * b8fa72ce -- facade tests fixed
|/  
* bda481d7 -- Swagger DTO for find all training instances corrected. Just Swagger docs change.
*   21bc3994 -- Merge branch '437-get-rid-of-calling-python-api-when-calling-get-all-training-instances' into 'master'
|\  
| * 1e73f2eb -- Get rid of calling python API for getting assigned sandboxes with training runs when getting all training instances.
|/  
* d7f9cc46 -- Update pom.xml version based on GitLab tag. Done by CI
*   66019463 -- Merge branch '435-remove-while-cycle-from-getreadysandboxinstanceref-method' into 'master'
|\  
| * 4bd8586b -- Resolve "Remove while cycle from getReadySandboxInstanceRef method"
|/  
*   eac97ca0 -- Merge branch '436-change-rights-in-method-getuserrefdtobyuserrefid' into 'master'
|\  
| * a01071f9 -- Changed right to trainee in method
|/  
*   26c4faaf -- Merge branch '434-repair-sql-query-findallfororganizersunreleased' into 'master'
|\  
| * 0f618ed9 -- Query repaired
|/  
*   646e3e0e -- Merge branch '432-update-javadoc' into 'master'
|\  
| * d6589f33 -- Resolve "Update javadoc"
|/  
*   6db21b5a -- Merge branch '433-change-value-passed-to-super-method-in-trainingdefinitionrepositoryimpl' into 'master'
|\  
| * 5e6a176e -- Change value passed in constructor of TrainingDefinitionRepositoryImpl and smiplified method
|/  
*   2b52d157 -- Merge branch '430-fix-training-definition-repository-test' into 'master'
|\  
| * 5d6a7a26 -- persistence tests fixed
|/  
* 83d756ab -- rename kypo2-training-prod.properties to kypo2-training.properties
* 1a6f59a0 -- Update rules
*   d7e266fe -- Merge branch '428-create-dev-and-prod-properties-templates' into 'master'
|\  
| * 09d9b312 -- example properties files added
|/  
* e1599119 -- Update pom.xml version based on GitLab tag. Done by CI
* 3157b6cc -- Update README.md
*   9b69ed84 -- Merge branch '426-refactor-findall-to-do-not-create-query-with-joins-etc-as-predicate' into 'master'
|\  
| * 05aeae38 -- Resolve "Refactor findAll to do not create query with joins etc as predicate"
|/  
*   1388cf2c -- Merge branch '427-change-messages-in-logs-when-get-error-response-from-pythonapi-or-userandgroup' into 'master'
|\  
| * e4a89afe -- Modified messages in logs and exceptions when get error from some other API
|/  
*   26ef230d -- Merge branch '424-api-response-property-adjustment-in-rest-controllers' into 'master'
|\  
| * 883d424e -- Resolve "Api response property adjustment in rest controllers"
|/  
* e4f1227b -- Update pom.xml version based on GitLab tag. Done by CI
*   5de074bc -- Merge branch '425-fix-wrong-authorization-for-getting-all-training-definitions-for-organizator' into 'master'
|\  
| * 365fb886 -- Resolve "Fix wrong authorization for getting all training definitions for organizator"
|/  
*   ea5cf9b9 -- Merge branch '423-api-model-property-adjustment-in-dtos' into 'master'
|\  
| * b068cec3 -- Add custom swagger reader for generating snake_case.
|/  
*   6121fbe5 -- Merge branch '422-modify-private-method-in-auditeventsservice-to-not-obtain-user-info-from-uag' into 'master'
|\  
| * 6c16dac6 -- Removed some attributes from AuditInfoDTO
|/  
* 11351759 -- Update README.md
* 4ab753cb -- Update README.md
* 7e6a0daa -- Update pom.xml version based on GitLab tag. Done by CI
* 03fdca0c -- Security-commons upgraded to 1.0.9 version.
* e524a61f -- Update pom.xml version based on GitLab tag. Done by CI
* fdb9a6a2 -- Move to 1.0.8.
* 24afe9ce -- Update security-commons version to 1.0.6.
*   2ea02824 -- Merge branch '421-error-check-update-in-sandbox-deletion-method' into 'master'
|\  
| * f95e937c -- error check updated in sandbox deletion method
* |   5ad0f2a1 -- Merge branch '420-for-organizers-not-filtered-based-on-state' into 'master'
|\ \  
| |/  
|/|   
| * 3d1dedf9 -- Resolve "for-organizers not filtered based on state"
|/  
* c99fc8f0 -- Update pom.xml version based on GitLab tag. Done by CI
* cd8145bb -- Security-commons newer version.
*   73e2d8c6 -- Merge branch '414-dockerize-this-project' into 'master'
|\  
| * 3cb054ca -- Resolve "Dockerize this project"
|/  
* 660fedcf -- Update pom.xml version based on GitLab tag. Done by CI
* 71f9ae30 -- Renew security-commons version.
* f5d4408b -- Update pom.xml version based on GitLab tag. Done by CI
* 702aba91 -- Security commons version updated.
*   a71263d2 -- Merge branch '419-provide-abstractentity-to-set-id-columns-as-mappedsupper-class-in-jpa' into 'master'
|\  
| * f457af97 -- Format all the entity classes.
|/  
*   b15a25a6 -- Merge branch '418-fix-http-status-checks' into 'master'
|\  
| * 5f8ed24a -- http status checks fixed
|/  
*   368b65d2 -- Merge branch '417-divide-access-method-in-service' into 'master'
|\  
| * 5399a766 -- access method divided in service
|/  
* 90b32069 -- Update pom.xml version based on GitLab tag. Done by CI
*   9f2ffd8e -- Merge branch '416-create-trainingrunacquisitionlock-entity' into 'master'
|\  
| * 94b3e1a7 -- Resolve "Create TrainingRunAcquisitionLock entity"
|/  
*   5e2dfcf5 -- Merge branch '410-wrap-python-error-messages-to-our-exception-handler' into 'master'
|\  
| * c3ca4b02 -- Resolve "Wrap Python Error Messages To Our Exception Handler"
|/  
*   b47abbfc -- Merge branch '415-two-same-hints-are-not-being-saved-into-game-level' into 'master'
|\  
| * 3a92dbfb -- Added field order to hashCode and equals methods in HintDTO
|/  
*   d7cf199a -- Merge branch '407-create-new-endpoint-to-retrieve-all-users-needed-for-vizualization' into 'master'
|\  
| * 14e38fe2 -- Resolve "Create new endpoint to retrieve all users needed for vizualization"
|/  
*   cffdcd07 -- Merge branch '403-modify-export-of-training-definitons-instances-and-theirs-users' into 'master'
|\  
| * feb2820f -- Resolve "Modify export of training definitons, instances and theirs users"
|/  
*   47fcd758 -- Merge branch '400-remove-field-family-name-given-name-full-name-from-audited-events' into 'master'
|\  
| * ec1689d6 -- Resolve "Remove field family name, given name, full name from audited events"
|/  
*   5cea4f6e -- Merge branch '372-include-pagination-for-designers-and-organizers-endpoints' into 'master'
|\  
| * 5b3d9a48 -- Resolve "Include pagination for designers and organizers endpoints"
|/  
*   2acc40e4 -- Merge branch '395-remove-attributes-from-userref-entity' into 'master'
|\  
| * acffebc5 -- Resolve "Remove attributes from UserRef entity"
|/  
*   42d05781 -- Merge branch '413-repair-access-training-run-method-in-service-layer' into 'master'
|\  
| * e7769e0e -- Repaired access training run and locking of sandboxes.
|/  
*   9c9fea98 -- Merge branch '412-traininginstance-update-removes-poolid-attribute' into 'master'
|\  
| * 728752a3 -- trainingInstance update no longer erases poolId
|/  
*   b11d44a3 -- Merge branch '411-change-penalty-in-audit-of-solutiontaken-event' into 'master'
|\  
| * 75e995da -- penalty audit of SolutionTaken corrected
|/  
*   3895a6b5 -- Merge branch '402-pool-should-be-created-with-creation-of-instance' into 'master'
|\  
| * a709bd69 -- Resolve "Pool should be created with creation of instance"
|/  
*   ace34308 -- Merge branch '409-create-attachements-entity' into 'master'
|\  
| * b9a5813d -- Attachment entity added
|/  
*   c7299102 -- Merge branch '401-support-drag-and-drop-swapping-levels-in-training-defnition' into 'master'
|\  
| * 9c6ee0e6 -- Resolve "Support drag and drop swapping levels in training defnition"
|/  
*   b4baafee -- Merge branch '389-get-rid-of-sandboxinstanceref-entity' into 'master'
|\  
| * a9aff189 -- Resolve "Get rid of SandboxInstanceRef entity"
|/  
* ada60cd3 -- Set spring.jpa.show-sql to false by default in kypo2-training.properties.
*   7ed91198 -- Merge branch '398-set-encoding-for-logback' into 'master'
|\  
| * 82a13b29 -- Encoding of logback has been set.
|/  
*   5814450b -- Merge branch '397-repair-obtaining-sandbox-info-from-pythonapi' into 'master'
|\  
| * e5d7c080 -- Obtaining sandbox info from PythonAPI fixed
|/  
*   43ae0c46 -- Merge branch '396-add-attribute-picture-to-userinfodto' into 'master'
|\  
| * 7831c642 -- Picture attribute added to the UserInfoDTO
|/  
*   c990c598 -- Merge branch '388-fix-calls-to-django-openstack-api-to-support-pagination' into 'master'
|\  
| * d7ed928c -- Resolve "Fix calls to django-openstack API to support pagination"
|/  
*   dff1c38f -- Merge branch '394-change-banner' into 'master'
|\  
| * 0802d7d0 -- banner changed
|/  
* e7271db0 -- Update pom.xml version based on GitLab tag. Done by CI
*   3b2400dc -- Merge branch '393-visualizations-throws-error-and-don-t-display-data' into 'master'
|\  
| * 842ae949 -- correct data are audited now
|/  
* 501ca236 -- Update pom.xml version based on GitLab tag. Done by CI
*   10284c70 -- Merge branch '392-synchronized-access-training-run' into 'master'
|\  
| * 5f541627 -- Resolve "Synchronized access training run"
|/  
*   16d4a16d -- Merge branch '390-points-are-not-calculated-properly-when-solution-is-displayed' into 'master'
|\  
| * 5fd4f069 -- solution penalization is now corresponding with specification
|/  
* 687264a9 -- Update pom.xml version based on GitLab tag. Done by CI
*   f67167f8 -- Merge branch '371-misleading-incomplete-error-message-for-expired-trainings' into 'master'
|\  
| * 68f2b31a -- access training run error message changed
|/  
* a290a250 -- Update pom.xml version based on GitLab tag. Done by CI
*   a17d505f -- Merge branch '382-order-of-hints-is-changing-when-editing-a-training-definition' into 'master'
|\  
| * 9e54082b -- Resolve "Order of hints is changing when editing a training definition"
|/  
* a244818b -- Update kypo2-training.properties with cors.allowed.origins
*   f23f08f0 -- Merge branch '387-user-cannot-resume-last-level' into 'master'
|\  
| * 44f39d2c -- Support for RESUME state on back-end.
|/  
*   11144194 -- Merge branch '386-querydsl-evaluate-contains-query-for-strings' into 'master'
|\  
| * bd1cb249 -- support for querydsl contains query.
|/  
*   beee7d2c -- Merge branch '385-add-possibility-to-delete-events-by-training-definition-training-instance-and-training-run-ids' into 'master'
|\  
| * 9f6a6bcb -- The possibility to delete training runs added.
|/  
*   c3c755fc -- Merge branch '383-add-userrefid-to-audits' into 'master'
|\  
| * cfe545d9 -- Resolve "Add UserRefId to Audits"
|/  
*   9cac44c5 -- Merge branch '381-update-testing-data-for-elastic' into 'master'
|\  
| * 37a63b5b -- elasticsearch testing data updated
* |   79bfe817 -- Merge branch '380-calculation-of-the-player-score-must-be-redesigned' into 'master'
|\ \  
| |/  
|/|   
| * 984edb7f -- Resolve "Calculation of the player score must be redesigned"
|/  
*   67aa64ab -- Merge branch '379-same-sandbox-is-added-to-multiple-trainees' into 'master'
|\  
| * 767f59c8 -- access training run has isolation set to serializable
|/  
* be57e4b7 -- make access training run synchronized.
* 896ff190 -- countQueries updated.
*   e5886466 -- Merge branch '378-create-synchronization-endpoint' into 'master'
|\  
| * 57a6af42 -- Resolve "Create synchronization endpoint"
|/  
*   30b535d6 -- Merge branch '374-fix-integration-tests' into 'master'
|\  
| * 0e232342 -- Resolve "Fix integration tests"
|/  
* 1ea6c3a9 -- Update pom.xml version based on GitLab tag. Done by CI
*   fe12057d -- Merge branch '377-create-unique-contraint-on-sandbox-instance-ref-id' into 'master'
|\  
| * 617a5125 -- Added constraint -  sandbox instance ref id is unique
|/  
* 095d5261 -- Update pom.xml version based on GitLab tag. Done by CI
*   a0190443 -- Merge branch '376-reduce-calling-user-and-group-when-need-to-obtain-user-ref-id' into 'master'
|\  
| * eafba35a -- Change some security service methods invocations and removed unused seceurity service bean from facade layer
|/  
* 40d04d4c -- Update pom.xml version based on GitLab tag. Done by CI
*   d4222a0e -- Merge branch '375-repair-bug-error-when-calling-endpoint-get-td-for-organizers' into 'master'
|\  
| * fa3a7c52 -- Repaired service method findAllForOrganizers and repository method findAllForDesignersAndOrganizers
|/  
* 923b77b0 -- Update pom.xml version based on GitLab tag. Done by CI
* 282b4b83 -- Multiple OIDC Identity Providers configuration added, also the configuration for enabling or disabling swagger added and also the configuration for setting CORS urls added.
*   94f44922 -- Merge branch '368-add-check-of-pool-before-accessing-training-run' into 'master'
|\  
| * 56dcf614 -- Resolve "Add check of pool before accessing Training Run"
|/  
*   6fc57232 -- Merge branch '373-fix-service-layer-unit-tests' into 'master'
|\  
| * aea8b42b -- Resolve "Fix service layer unit tests"
|/  
*   66c29735 -- Merge branch '370-redesign-training-project-for-two-oidc-providers' into 'master'
|\  
| * 17eb193b -- Resolve "Redesign training project for two OIDC providers."
|/  
*   d5f0dae0 -- Merge branch '369-check-pool-before-creating-new-pool-for-training-instance' into 'master'
|\  
| * e22d5dda -- sandboxPool is checked in openstack before creation
|/  
*   a219d320 -- Merge branch '367-check-if-pool-exists-in-pythonapi-in-synchronization-method' into 'master'
|\  
| * 83df3db3 -- Pool is checked when delete training instance
|/  
* c9302256 -- Update pom.xml version based on GitLab tag. Done by CI
*   d5732a56 -- Merge branch '366-delete-events-from-training-run-when-training-run-is-deleted' into 'master'
|\  
| * cb368fb8 -- Resolve "Delete events from training run when training run is deleted"
|/  
*   5cca1acf -- Merge branch '365-check-sandboxes-in-openstack-when-delete-training-instance' into 'master'
|\  
| * a92c6ac8 -- Sandboxex are checked and synchronized before delete training instance
|/  
*   b99e9f9f -- Merge branch '364-delete-data-from-elastic-after-instance-archivation' into 'master'
|\  
| * dee8b31d -- events are deleted after archivation
|/  
*   6cbf0a84 -- Merge branch '363-change-ssl-protocol-in-rest-template' into 'master'
|\  
| * 1d9aa48a -- Chagned version of protocol for SSL handshake
|/  
*   7a1bd5c0 -- Merge branch '362-check-db-consistency-before-sandbox-allocation' into 'master'
|\  
| * 1aee49e3 -- DB consistency is checked before allocation
|/  
*   8744ac99 -- Merge branch '361-archive-training-instances-with-events' into 'master'
|\  
| * a7fc91a6 -- Resolve "Archive training instances with events"
|/  
* cce38c5d -- Update pom.xml version based on GitLab tag. Done by CI
*   ba2821b3 -- Merge branch '360-import-td-wont-import-levels' into 'master'
|\  
| * 61e498af -- exported levels map correctly for import
|/  
*   cfad1706 -- Merge branch '359-if-training-run-is-closed-on-last-level-it-cannot-be-resumed' into 'master'
|\  
| * bc93f6b2 -- training run can be resumed on last level
|/  
* ca92b691 -- Change version listing
* 47d3ebe0 -- Update pom.xml version based on GitLab tag. Done by CI
*   6bcd942e -- Merge branch '358-get-visualization-info-for-given-training-instnace-not-for-training-run' into 'master'
|\  
| * f9df8002 -- Created two endpoints to get visualization info for training instance or training run
|/  
* 2451dcf7 -- Update pom.xml version based on GitLab tag. Done by CI
*   3b1efc93 -- Merge branch '345-it-must-be-possible-to-force-delete-training-instance-and-training-run' into 'master'
|\  
| * 55b12bca -- Resolve "It must be possible to force delete training instance and training run"
|/  
*   ba150f25 -- Merge branch '357-change-versions-of-some-dependencies-and-resolve-build-warnings' into 'master'
|\  
| * e991ede9 -- Resolved issues with mapper and modified version of some dependencies
|/  
*   be7771d1 -- Merge branch '356-check-sandbox-in-openstack-when-delete-training-run' into 'master'
|\  
| * 49dfc9a0 -- Resolve "Check sandbox in openstack when delete training run"
|/  
*   e511d64a -- Merge branch '355-fix-elastic-template' into 'master'
|\  
| * a56c4b20 -- Resolve "Fix elastic template"
|/  
* 544b6202 -- Update pom.xml version based on GitLab tag. Done by CI
* a5417323 -- Update postinst
* 181d0203 -- Update install
* 42f576e5 -- Update control
* 0d239ae8 -- Update service
* c00f2498 -- Update install
* 7555b11b -- Update kypo2-training.properties
* 02508799 -- Update kypo2-training.properties
* 31737d48 -- Update README.md
* c43727b4 -- Update kypo2-training.properties
* d9784621 -- kypo2-training.properties added.
*   49d5feba -- Merge branch '354-include-default-configuration-file-to-the-project-root-directory-for-debian-purposes' into 'master'
|\  
| * b3e106d1 -- property file configured more properly in README.md
|/  
* 69b4e429 -- Update pom.xml version based on GitLab tag. Done by CI
*   77e24059 -- Merge branch '353-endpoit-to-verify-flag-return-wrong-iscorrectflagdto' into 'master'
|\  
| * e2b515f7 -- incorrectFlagCount of TR resets to 0 with each next level
|/  
* e5eb4833 -- Edit service user
* e1077fa5 -- Update pom.xml version based on GitLab tag. Done by CI
*   2dcf6db9 -- Merge branch '352-make-deb-packages-lighter' into 'master'
|\  
| * 2c6ee44d -- Do not upload Deb package as artifact
| * 3ab840fa -- Do not upload Deb package as artifact
| * 66db2878 -- Install from jars, edit service file
* |   b0317c56 -- Merge branch '348-wrong-number-of-levels-in-training-run-overview' into 'master'
|\ \  
| * | 942830e6 -- level order now updates with every level deletion
|/ /  
* |   8c2eeb7e -- Merge branch '335-add-field-solution-into-iscorrectflagdto' into 'master'
|\ \  
| |/  
|/|   
| * dc30f3ce -- Added field solution to IsCorrectFlagDTO
|/  
*   9facb732 -- Merge branch '351-modify-method-delete-sandbox-in-traininginstanceserviceimpl' into 'master'
|\  
| * b04753a9 -- Resolve "Modify method delete sandbox in TrainingInstanceServiceImpl"
|/  
*   7cb14dbf -- Merge branch '349-sandbox-is-not-deleted-in-db-when-it-has-assigned-training-run' into 'master'
|\  
| * e3a2f1dc -- Sandboxes which are assigned to the TR can be deleted
|/  
*   7a0a4c68 -- Merge branch '344-add-endpoint-to-get-info-about-levels-for-specific-training-run' into 'master'
|\  
| * 94ead2aa -- Added endpoint for vizualizations
|/  
* f7623ac4 -- Fix tests to the new final state.
*   185e11f4 -- Merge branch 'master' of gitlab.ics.muni.cz:kypo2/services-and-portlets/kypo2-training
|\  
| *   3f3950f6 -- Merge branch '347-possibility-to-delete-training-run' into 'master'
| |\  
| | * ec2b0aa5 -- Resolve "Possibility to delete training run"
| |/  
* | cf005fa8 -- SandboxStates added.
|/  
*   c2821d3b -- Merge branch '346-fix-resume-training-run-action' into 'master'
|\  
| * 427d8c7f -- It is possible to resume to the training run.
|/  
* 4d80412d -- Add author tag.
* c14bc2a6 -- Audit level title correctly.
* 52b3c730 -- Return full info about users.
*   c0ef2d22 -- Merge branch '343-fix-elastic-search-template-script' into 'master'
|\  
| * f1575787 -- set-elasticsearch-template script and template updated
|/  
*   5a57e361 -- Merge branch '341-enable-https-in-spring-boot-project' into 'master'
|\  
| * bb98c3a5 -- Resolve "Enable HTTPS in Spring Boot project"
|/  
* 305f3001 -- Create Deb package in deployDebian stage
* bcb781b4 -- README for generating SSL added.
*   180f155a -- Merge branch '340-fix-updatetraininginstance-unit-test' into 'master'
|\  
| * face2996 -- updateTrainingInstance service unit test fixed
|/  
* e83a617c -- Update pom.xml version based on GitLab tag. Done by CI
*   a0861326 -- Merge branch '339-allow-trainee-access-training-definition-with-given-id' into 'master'
|\  
| * dea32cb2 -- Trainee is allowed to get training definition with given ID
|/  
* 71a0111f -- Repaired request JSON when create pool for sandboxes
*   9acfb6c4 -- Merge branch '337-automaticaly-add-logged-in-user-as-organizer-to-updated-training-instance' into 'master'
|\  
| * 5cee9d47 -- Logged in user is automaticaly added as organizer when update training instance
|/  
*   15c3485a -- Merge branch '338-update-logback-spring-xml' into 'master'
|\  
| * 22c3d817 -- logback updated
|/  
*   ff95867e -- Merge branch '336-modify-handling-error-responses-from-rest-template-when-calling-pythonapi-or-userandgroup' into 'master'
|\  
| * 71010a97 -- Changed the way how to handle errors from rest template.
|/  
* cc65c8f4 -- Set script for inserting an Elasticsearch template.
*   b3707ea4 -- Merge branch '334-add-javadoc-to-rest-layer' into 'master'
|\  
| * 29ac2128 -- Resolve "Add javadoc to rest layer"
|/  
*   893f7c26 -- Merge branch '332-integrate-logging-aspect' into 'master'
|\  
| * 7a0ea27b -- AOP clases integrated into project
|/  
*   2f669a50 -- Merge branch '330-add-javadoc-to-api-layer' into 'master'
|\  
| * 0e8ed6cf -- Resolve "Add javadoc to api layer"
|/  
* e3097754 -- Fix typo
*   1dedeb81 -- Merge branch '333-fix-wrong-error-message-in-tests' into 'master'
|\  
| * 82c48f8c -- Fix expected messages in unit tests.
|/  
*   902dfcc3 -- Merge branch '331-repair-and-fix-todo-issues-in-project' into 'master'
|\  
| * 24d08879 -- Cannot resume archived TR, switch TR state from one to the same one will not cause error, fixed unit test for accessTrainingRun, two different errors when finis non last or not answered level
|/  
*   a2e94241 -- Merge branch '329-impossible-to-delete-sandbox-after-failed-deletion' into 'master'
|\  
| * 03be794d -- Resolve "Impossible to delete sandbox after failed deletion"
|/  
*   b65c717c -- Merge branch '325-fix-and-update-integration-tests' into 'master'
|\  
| * 46e90742 -- Resolve "Fix and update integration tests"
|/  
*   64bbd5b1 -- Merge branch '327-modify-sandboxes-endpoint-for-full-build' into 'master'
|\  
| * b4dcbf33 -- query for full sandbox build fixed
|/  
* 7460073f -- Dont export Deb package as GitLab CI artifact
*   ccb44dab -- Merge branch '328-edit-debian-postinstall-script' into 'master'
|\  
| * b14fe70f -- Edir debian/rules
* |   63ad4470 -- Merge branch '326-add-javadoc-to-service-layer' into 'master'
|\ \  
| |/  
|/|   
| * b922e54d -- Resolve "Add javadoc to service layer"
|/  
*   1ec87495 -- Merge branch '327-modify-sandboxes-endpoint-for-full-build' into 'master'
|\  
| * 70bd74af -- Resolve "Modify sandboxes endpoint for full build"
|/  
*   b067a62d -- Merge branch '324-fix-existsanyfortrainingdefinition-query' into 'master'
|\  
| * 1ad6dd98 -- training instance exitsAnyForTrainingDefinition query fixed
|/  
*   2da467d6 -- Merge branch '322-accesible-training-runs-returning-incorrect-data' into 'master'
|\  
| * f83bcbdf -- sorting of accesible runs by title moved from rest to facade
* |   8f4ff314 -- Merge branch '322-accesible-training-runs-returning-incorrect-data' into 'master'
|\ \  
| |/  
| * 39a8d756 -- +1 added to current level in accessible trainig runs DTO
* |   2ed722eb -- Merge branch '320-add-javadoc-to-persistance-layer' into 'master'
|\ \  
| |/  
|/|   
| * ba0a24f0 -- Resolve "Add javadoc to persistance layer"
|/  
*   bba44a30 -- Merge branch '321-change-field-organizers-in-traininginstancecreatedto-and-traininginstanceupdatedto-to-organizers_login' into 'master'
|\  
| * 521b396b -- Field in TrainingInstanceCreateDTO organizers changed to list of strings organizersLogin
|/  
*   cfd8c80d -- Merge branch '319-add-field-sandboxes_with_training_run-into-traininginstancedto' into 'master'
|\  
| * 36a6a411 -- Resolve "Add field sandboxes_with_training_run into TrainingInstanceDTO"
|/  
*   f8028d7a -- Merge branch '318-add-parameter-active-to-endpoint-for-obtaining-all-training-runs' into 'master'
|\  
| * bc902ed7 -- Add parameter isActive into endpoint for obtaining training runs for specific training instance
|/  
*   80072131 -- Merge branch '317-create-endpoint-for-reallocating-sandbox-of-given-training-run' into 'master'
|\  
| * 9bab529d -- Changed states of Training run and state is changed to archived when sandbox is deleted.
|/  
*   b27f99f6 -- Merge branch '315-change-createtrainingdefinitiondto-to-accept-list-of-logins-instead-of-list-of-authors' into 'master'
|\  
| * 5c5522e9 -- Object authors and organizers in update and create training definition DTO changed to list of logins
|/  
*   7b8b841b -- Merge branch '311-accessed-training-run-number-of-levels-attribute-has-wrong-value' into 'master'
|\  
| * e078ce02 -- added +1 to number of levels assigned to accessed runs
|/  
*   fb66692e -- Merge branch '313-sort-on-title-of-trainee-accessed-trs-returns-error' into 'master'
|\  
| * b72f68fa -- Resolve "Sort on title of trainee accessed TRs returns error"
|/  
*   5a80138b -- Merge branch '314-redesign-oidc-information-about-authenticated-user-to-enum-instead-of-string-to-prevent-string-mistakes' into 'master'
|\  
| * bd93a91e -- Enums instead of Strings for enumerative values provided.
|/  
*   5d990dc4 -- Merge branch '312-given-and-family-name-are-null-in-user-ref-dtos' into 'master'
|\  
| * 461c5e10 -- Family name and given name attribute are obtaining from user and group
|/  
*   cfa2e13d -- Merge branch '310-cannot-get-ti-by-its-id' into 'master'
|\  
| * 1a4c1b4c -- Resolve "Cannot GET TI by its id"
|/  
*   f0cd7315 -- Merge branch '309-resumption-of-trainin-run-after-taking-hint-fails' into 'master'
|\  
| * a64e5c19 -- Taken hints are returned, excpetion from hibernate solved
|/  
*   a34ffde9 -- Merge branch '306-add-full_name-given_name-and-family_name-into-audit-events' into 'master'
|\  
| * 99bdfb8f -- Added full name and full name without title into audit events
|/  
*   e16441f4 -- Merge branch '305-create-class-hintinfo-for-taken-hints-in-training-run' into 'master'
|\  
| * 481f985a -- Added TakenHintDTO into AbstractLevelDTO and remove snapshothook
|/  
*   cd0d11a0 -- Merge branch '291-optimize-sql-selects' into 'master'
|\  
| * f8aba4ce -- Faulty unit tests fixed
| *   703d89f8 -- merge with master
| |\  
| |/  
|/|   
* |   862c2aad -- Merge branch '288-impossible-to-access-training-run-after-reallocation-of-sandbox' into 'master'
|\ \  
| * | 3f0ab12e -- Problem with access training run after reallocation of sandbox solved.
|/ /  
* |   1a258477 -- Merge branch '287-no-error-message-when-resuming-and-sandbox-is-deleted' into 'master'
|\ \  
| * | 3d610eeb -- Exception with adequate message is thrown when resume training run without sandbox.
|/ /  
* |   39ed3672 -- Merge branch '304-add-role-description-in-config-file-roles-json' into 'master'
|\ \  
| * | 6bffe402 -- Config file roles.json modified
|/ /  
* |   dc0e4476 -- Merge branch '303-update-erd-diagram' into 'master'
|\ \  
| * | 2654eadb -- ERD fixed
* | |   62314311 -- Merge branch '303-update-erd-diagram' into 'master'
|\ \ \  
| |/ /  
| * | 97c3b6a6 -- ERD updated
|/ /  
* |   3ff0abbf -- Merge branch '302-add-given-name-and-family-name-to-user' into 'master'
|\ \  
| * | 7b55e771 -- Resolve "Add given name and family name to user"
|/ /  
* |   8bf69d04 -- Merge branch '294-select-name-when-cloning-td' into 'master'
|\ \  
| * | 00bd09bf -- added title param to clone training definition endpoint
* | |   3bcd8a23 -- Merge branch '301-remove-property-file-from-deb-package' into 'master'
|\ \ \  
| * | | 0ff50ce4 -- Remove property file
|/ / /  
* | | 1354c600 -- State added to TD DTO for organizers
|/ /  
* | 9b0df52d -- Estimated duration set when updating training definition.
* |   57e22cfb -- Merge branch '293-start-time-of-training-run-is-null' into 'master'
|\ \  
| * | b3a34c7a -- Resolve "Start time of training run is null"
|/ /  
* |   1fc2c963 -- Merge branch '296-source-code-inserted-into-solution-in-game-level-is-not-allowed' into 'master'
|\ \  
| * | bc9c4e4f -- change JPA types.
|/ /  
* |   edc388ed -- Merge branch '284-add-service-and-config-files-into-deb-package' into 'master'
|\ \  
| * | afcfefae -- Edit service file
| * | aeabb8b1 -- Install jars within Deb package
| * | ae3359ee -- Fix path to the config file
| * | 17f9eadf -- Add service file and config file to the Deb package
* | |   76661135 -- Merge branch '292-add-boris-jadus-author-tag-to-selected-classes' into 'master'
|\ \ \  
| * | | 08166fe9 -- Resolve "Add Boris Jadus author tag to selected classes"
|/ / /  
| | * 70e57fc7 -- find instance by Id IT fixed after refactor
| | * b53e5777 -- find training run by id optimized
| | * 685fd3a6 -- accessTrainingRun service unit tests fixed after refactor
| | * 6a5ca901 -- accessTrainingRun() optimized
| | * f018003d -- training instance query selects optimized
| | * 30745aa9 -- TrainingDefinitionIT findLevelById tests fixed
| | * fa51db03 -- training definition query selects optimized
| | * 61cd7661 -- findDefinitionById query optimized
| |/  
|/|   
* |   509fb8c7 -- Merge branch '289-allow-specification-of-sandbox-allocation-count' into 'master'
|\ \  
| * | 2eadc1e5 -- optional attribute count added to allocateSandboxes endpoint
|/ /  
* |   7e3f9291 -- Merge branch '285-add-attributes-to-accesstrainingrundto' into 'master'
|\ \  
| * | 78534c49 -- startTime attribute added to AccessTrainingRunDTO class
|/ /  
* |   f899fd1c -- Merge branch '290-add-instance-and-definition-id-to-trainign-run' into 'master'
|\ \  
| * | a804cc8f -- Resolve "Add instance and definition id to trainign run"
|/ /  
* |   36e3d3ab -- Merge branch '286-change-implicit-value-of-incorrect-flag-limit' into 'master'
|\ \  
| * | 9c0dc909 -- implicit incorrectFlag attribute of GameLevel changed from 0 to 100
|/ /  
* | 3cfbc1ea -- Add endpoint for retrieving elasticsearch events from specific training run.
* |   78b310d4 -- Merge branch '283-configure-project-to-send-logs-into-syslog-with-correct-parameters' into 'master'
|\ \  
| |/  
|/|   
| * 9107e28b -- New config of logback
|/  
*   ab4c2ebf -- Merge branch '280-fix-deletion-of-sandboxes' into 'master'
|\  
| * 62b0c62e -- Delete sandboxes repaired
|/  
*   9755e26c -- Merge branch '279-create-one-default-question-with-new-assessment-level' into 'master'
|\  
| * b2cd9c3e -- Default question for assessment created
|/  
*   068a3740 -- Merge branch '281-remove-try-again-from-possible-actions-of-training-run' into 'master'
|\  
| * 2502e6f8 -- TRY_AGAIN action removed from accessTrainingRunDTO class
* |   107a0d38 -- Merge branch '278-add-new-state-to-training-run' into 'master'
|\ \  
| |/  
|/|   
| * 5a9455e4 -- Training run state ARCHIVED renamed to FINISHED
|/  
*   4309cbd2 -- Merge branch '248-configure-syslog-and-logstash-to-save-events-into-database' into 'master'
|\  
| * f7ed0fd1 -- Added configuration to logstash
|/  
*   433ac655 -- Merge branch '276-cannot-take-hint-in-training-run-game-level' into 'master'
|\  
| * 6f5f9824 -- JsonProperty value fixed for hintId attribude of HintTaken event class
|/  
*   da44534f -- Merge branch '277-add-training-instance-id-to-trainee-endpoints' into 'master'
|\  
| * c8a7b8e7 -- instanceId attribute added to AccessTrainingRunDTO and AccessedTrainingRunDTO classes
|/  
*   ee4a2ffd -- Merge branch '275-fix-transferring-token-between-asynchronous-tasks-in-spring' into 'master'
|\  
| * ce4be499 -- Resolve "Fix transferring token between asynchronous tasks in Spring"
|/  
*   e84902a3 -- Merge branch '274-sort-by-estimated-duration-does-not-change-order' into 'master'
|\  
| * 7fc4e682 -- Sort TD by estimated duration does change order in page
|/  
*   2e30cff5 -- Merge branch '273-info-about-levels-attribute-is-empty-on-resume-training-run' into 'master'
|\  
| * 938c79d8 -- Resolve "Info about levels attribute is empty on resume training run"
|/  
* 5304cd97 -- Update pom.xml version based on GitLab tag. Done by CI
* 1e0d070e -- Run CI as Kyposervice
* 90ea8200 -- Update pom.xml version based on GitLab tag. Done by CI
*   c6e6f815 -- Merge branch '259-fix-import-of-training-definition' into 'master'
|\  
| * e9ac2bf4 -- training definition import fixed
* | d7562302 -- Update pom.xml version based on GitLab tag. Done by CI
* | 14fb78dd -- Rollback to older version to force a new pipeline
* | 3059379a -- Rollback to older version to force a new pipeline
* | d17efee7 -- Switch to parent directory after sec-commons mvn install
* | dec32f0c -- Update pom.xml version based on GitLab tag. Done by CI
* | 6e84ac7b -- Fix CI sed command
* |   92466110 -- Merge branch '265-enable-cd-to-master' into 'master'
|\ \  
| * | 7b5d9843 -- Dont run tests on mvn deploy
| * | ddf1751b -- Edit the editVersion phase
| * | 2627ca23 -- Ignore .gitlab-ci.yml file during grep+sed
* | | ac38b3e2 -- refactor security annotations.
* | | 4fe59859 -- context copying decorator added.
* | | ee53954c -- authorization token created as new string from httpservletrequest.
* | | 49be297c -- Remove twice Bearer string in authorization method.
* | | 7f066934 -- pass http headers from facade layer to set token before async method.
* | | b5d92e2c -- Use default @Async with process executor.
* | | 50715b95 -- add task executor with new security context holder in spring security.
* | | d6e53692 -- Call default Async operation instead of properiatary process executor Async TaskExecutor.
* | | 3fc8c5e5 -- preauthorize add to facade layer when calling allocation and deletion of sandboxes.
* | |   9b074783 -- Merge branch '271-check-security-roles-run-in-new-transaction' into 'master'
|\ \ \  
| * | | 2d79f453 -- add security checks to a new transaction.
|/ / /  
* | |   202254e3 -- Merge branch '268-fix-sorting-of-training-definitions-based-on-estimated-duration-and-last_edit-review-the-querydsl-and-pageable-options' into 'master'
|\ \ \  
| * | | c041160f -- training definition contains estimated duration attribute.
|/ / /  
* | |   7a879711 -- Merge branch '267-unpossibility-to-change-state-from-released-to-unreleased-when-the-training-instance-is-associated-to-the-particular-training-definition' into 'master'
|\ \ \  
| * | | 4933a2fa -- update possiblity to change state from released to unreleased.
|/ / /  
* | |   a3ba8dce -- Merge branch '262-could-not-change-state-from-unreleased-to-released-when-training-instances-are-associated-fix' into 'master'
|\ \ \  
| * | | 43c28590 -- State can be changed from unreleased to released when TD have TI
|/ / /  
* | |   f18c2cf3 -- Merge branch '264-td-swapping-is-allowed-when-update-is-not' into 'master'
|\ \ \  
| * | | b77adb12 -- Levels in TD cannot be swapped when TD has assigned training instance
|/ / /  
* | | 59ca0a2f -- interceptor configuration is autowired in service config.
* | | 70368a52 -- resttemplate made as configuration class.
* | |   1724dd08 -- Merge branch '266-separate-service-config-class-for-copying-context-config-classes' into 'master'
|\ \ \  
| * | | f29e9974 -- reconfigure async operations.
|/ / /  
* | | 6814906b -- add decorators for Async operations.
|/ /  
* | 1dc477ac -- change security annotation on facade layer.
* | b0a93d1d -- add security to facade layer for allocate sandboxes to avoid transfering token between threads.
* |   87d051ec -- Merge branch '261-sort-returned-abstract-levels-in-training-definition' into 'master'
|\ \  
| * | 8b862545 -- return levels by training definition id sorted by order attribute.
|/ /  
* |   64669cf3 -- Merge branch '260-do-not-use-testing-data-migrate-db-without-testing-data' into 'master'
|\ \  
| |/  
|/|   
| * 09052de7 -- delete testing data.
|/  
*   b93b4991 -- Merge branch '252-redesign-database-to-add-relation-between-training-definition-and-abstract-level' into 'master'
|\  
| * 04e63f50 -- redesign service, facade and rest layer for including the relation between training definition and abstract level.
|/  
*   c3f48332 -- Merge branch '257-impossible-to-reallocate-after-deleting-sandbox-with-tr' into 'master'
|\  
| * 77bbfe9b -- Delete sandboxes which are assigned to training run
|/  
*   5341aec1 -- Merge branch '251-associated-training-definitions-returns-incorrect-can-be-edited-attribute' into 'master'
|\  
| * fcb4d1c0 -- Can be edited attribute removed from DTO
|/  
*   513f3d79 -- Merge branch '255-missing-hints-in-game-level-of-training-run' into 'master'
|\  
| * 1fb3123a -- List of hints added to GameLevelViewDTO
|/  
*   40a4da83 -- Merge branch '245-update-unit-tests' into 'master'
|\  
| * a4bd0f34 -- TrainingInstance service unit test for update and delete now check if start and end time are correct
|/  
* 3cf8635e -- change index name for audit actions in elasticsearch events.
*   311b754a -- Merge branch '246-use-methods-from-security-service-for-obtaining-sub-of-user' into 'master'
|\  
| * 1e923e7c -- Methods for sub or full name of user is moved to security service
|/  
* 6a301c55 -- change divison symbol to . in the ES index.
* f7fb4a70 -- elasticsearch index changed to using division symbol.
*   8500062d -- Merge branch '247-set-new-index-for-events-in-visualization-in-elasticsearch' into 'master'
|\  
| * 22d1be88 -- elasticsearch index changed.
|/  
*   0536890d -- Merge branch '242-td-clone-does-not-work' into 'master'
|\  
| * 6cb6e6f4 -- In cloned training definition, beta testing group is set to null
|/  
*   e188a5b3 -- Fix confict
|\  
| * 6012556d -- Typo in .gitlab-ci fix
| * 06c5e61a -- Add info texts to CI
| * 18dc8e29 -- Change pom.xml version deps recursively
| * 80e9a808 -- Dont install sec-comms in Debian Docker image
| * 01b61ffc -- Install security-commons as a pre-task
| * d404cf97 -- Edit rules file
| * 1967671c -- Debianize the project + enable CI
*   5c370aa2 -- Merge branch '241-delete-sandboxes-with-pool-when-deleting-training-instance' into 'master'
|\  
| * e7be9508 -- Delete sandbox pool when training instance is deleted
|/  
*   7f62221a -- Merge branch '243-assessment-level-has-incorrect-value-of-estimated-duration' into 'master'
|\  
| * 40113452 -- Estimated duration of new assessment level set to 1
|/  
*   f14a3fe4 -- Merge branch '219-add-audit-events-to-the-right-places' into 'master'
|\  
| * 57b76df5 -- Resolve "Add audit events to the right places"
|/  
*   29ed7155 -- Merge branch '232-deleting-td-with-associated-ti-throws-sql-constraint-error' into 'master'
|\  
| * 2eab3f85 -- Attempt to remove definition with created instance now throws meaningful error message
|/  
*   86414e2b -- Merge branch '240-update-td-in-ti-not-saved-in-db' into 'master'
|\  
| * 30c1d914 -- TI updates properly
* |   b10002b7 -- Merge branch '225-change-implicit-value-of-incorrect-flag-limit' into 'master'
|\ \  
| * | eeff17fb -- Resolve "Change implicit value of incorrect flag limit"
|/ /  
* |   744776b3 -- Merge branch '235-sandbox-allocation-does-not-work-properly' into 'master'
|\ \  
| |/  
|/|   
| * 5a929672 -- Allocate sandboxes reapired, possible to allocate sandboxes which left in pool
|/  
*   376428da -- Merge branch '236-filter-training-instances-user-can-see' into 'master'
|\  
| * 6550b689 -- Find all training instances returns only training instances of logged in organizer
|/  
*   ff486b8c -- Merge branch '231-create-endpoint-getalltdfordesigners-and-getalltdfororganizers' into 'master'
|\  
| * 3fb96d70 -- Resolve "Create endpoint GetAllTDForDesigners and GetAllTDForOrganizers"
|/  
*   dcd65f0f -- Merge branch '234-training-definition-call-does-not-return-levels' into 'master'
|\  
| * e50328ce -- levels removed from find all training definitions.
|/  
* 6d9cb611 -- add surefire plugin to all modules.
* e2cc6e6f -- surefire plugin ignore tests.
*   66eb7a13 -- Merge branch '233-downgrade-the-version-of-surefire-plugin-to-2-22-0-to-pass-the-tests' into 'master'
|\  
| * ee46e03a -- upgrade the surefire plugin.
|/  
*   e64e12b4 -- Merge branch '226-add-estimated-duration-column-to-designer-overview' into 'master'
|\  
| * 0338ab96 -- Resolve "Add estimated duration column to designer overview"
|/  
*   c2354015 -- Merge branch '222-refactor-and-investigate-the-resttemplate-to-include-the-token-for-sending-the-authenticated-user' into 'master'
|\  
| * dfe74971 -- Token is automatically added to header when using rest template
|/  
*   5de10877 -- Merge branch '227-add-last-edit-collumn-to-designer-overview' into 'master'
|\  
| * 1991592e -- Resolve "Add last edit collumn to designer overview"
|/  
*   18772013 -- Merge branch '224-openstack-400-bad-request-erro-when-allocate-sandboxes' into 'master'
|\  
| * da904655 -- Resolve "OpenStack:400 Bad Request erro when allocate sandboxes"
|/  
*   943c06d4 -- Merge branch '229-fix-export-td-and-ti-http-406-error' into 'master'
|\  
| * 1aed6eee -- rework exporting files to do it in memory not with temp file.
|/  
*   692920fe -- Merge branch '228-make-allocation-sandboxes-as-async-call' into 'master'
|\  
| * eb16604a -- allocate and reallocate of sandboxes are asynchronous operations now
|/  
*   2d8b8590 -- Merge branch '220-refactor-endpoints-for-export-to-design-them-not-to-return-json-dto-but-json-file-which-is-not-serialized-to-one-line' into 'master'
|\  
| * e272ab22 -- Resolve "Refactor endpoints for export to design them not to return json dto but json file which is not serialized to one line"
|/  
*   0b8fd4e1 -- Merge branch '223-authorize-annotations-in-training-instance-service-are-wrong' into 'master'
|\  
| * ff2e7fa8 -- Repaired preauthorize annotations in training instance service
|/  
*   ea2cbcb4 -- Merge branch '218-change-returned-size-of-documents-from-elasticsearch' into 'master'
|\  
| * 6ad9de2b -- index number of returned documents the limit set to 10000.
|/  
*   386e6de0 -- Merge branch '217-fix-script-to-upload-event-data-to-the-elasticsearch' into 'master'
|\  
| * 0ea0ab44 -- script to insert documents to the Elasticsearch added.
|/  
*   30da2a82 -- Merge branch '215-create-new-folder-for-events-and-split-events-to-them-modify-script-to-send-info-to-elastic-db' into 'master'
|\  
| * f23d0d3e -- Resolve "Create new folder for events and split events to them, modify script to send info to elastic DB"
|/  
*   545aa7d9 -- Merge branch '216-training-definition-find-by-id-return-the-list-of-levels-in-the-their-game-order' into 'master'
|\  
| * d91d2fee -- Change the list of authors from Set to List collection.
|/  
*   c3291255 -- Merge branch '206-add-another-parameter-for-visualizations-about-game-time-from-the-beginning' into 'master'
|\  
| * ca40f56a -- Resolve "Add another parameter for visualizations about game time from the beginning"
|/  
*   c29e69aa -- Merge branch '173-debianize-this-project' into 'master'
|\  
| * ac81d7f2 -- Resolve "Debianize this project"
|/  
*   900a5ec8 -- Merge branch '214-sort-events-by-timestamp-attribute' into 'master'
|\  
| * a14b7d4c -- get trainings events sorted by timestamp attribute.
|/  
*   76d005b7 -- Merge branch '212-new-index-for-elasticsearch-events' into 'master'
|\  
| * 1a59635d -- Match all documents under parametrized index.
|/  
*   c18bfc29 -- Merge branch '211-change-format-of-responses-to-emi-questions' into 'master'
|\  
| * 92baec0d -- Format of responses to EMI questions changed
|/  
*   11bf0d3b -- Merge branch '210-td-prefix-uploaded' into 'master'
|\  
| * b1003d9f -- Issue with set title and null pointer exception in test resolved.
| * f187e1d7 -- set prefix for uploaded training definitions.
| * 1810bf06 -- Uploaded prefix for uploaded training definitions.
|/  
*   f80df85d -- Merge branch '209-newly-imported-training-definition-should-have-state-unreleased-by-default' into 'master'
|\  
| * 5282d74a -- Resolve "Newly imported Training Definition should have state unreleased by default"
|/  
*   c390e142 -- Merge branch '201-cannot-get-all-training-definitions-when-user-have-role-designer-and-organizer' into 'master'
|\  
| * 04c08abf -- Merge branch 'master' into 201-cannot-get-all-training-definitions-when-user-have-role-designer-and-organizer
|/  
*   e70c92a8 -- Merge branch '208-released-td-should-be-editable-if-it-has-no-ti-created' into 'master'
|\  
| * f9ad311d -- Resolve "Released TD should be editable if it has no TI created"
|/  
*   43855d72 -- Merge branch '205-resolve-non-saving-hints' into 'master'
|\  
| * 70101000 -- Hints are being saved properly
* |   962ba215 -- Merge branch '207-remove-restraints-in-clone-method' into 'master'
|\ \  
| * | c2f18123 -- unreleased TD can be cloned now
|/ /  
* |   eea2efbf -- Merge branch '204-add-event-trainingrunsurrendered' into 'master'
|\ \  
| |/  
|/|   
| * 7b1d654d -- TrainingRunSurrendered even added.
|/  
* 8467501d -- Repair failing tests.
*   a2ee07b3 -- Merge branch '202-rename-rest-endpoint-for-exporting-training-instances' into 'master'
|\  
| * e61a41e0 -- rename rest endpoint for exporting training instances.
|/  
*   c63c07f9 -- Merge branch '199-archive-download-of-the-all-training-instance-data-a-user-can-archive-only-training-instances-which-passed-the-end-time' into 'master'
|\  
| * 01184f91 -- service unit tests added for archiveTrainingInstance method
| * ae13b2d2 -- facade unit tests added for archiveTrainingInstance method
| * 1b7c3169 -- controller unit tests added for ArchiveTrainingInstance method
| * a4b7127b -- archiveTrainingInstance now throws conflict exception correctly
| * cc816d45 -- only finished instances can be archived
| * 0da7e9aa -- Training Runs added to archiveTrainingInstance export
| * fa163653 -- archiveTrainingInstance() archives instance, definition and levels
| * 031793fe -- archive training instance endpoint created
| * 3e1bbc54 -- New DTO classes created for training instance archivation
|/  
*   387cb42e -- Merge branch '200-reallocate-sandboxes-should-not-allocate-whole-pool' into 'master'
|\  
| * eb3dee31 -- reallocate sandbox method now allocates only one sandbox
|/  
*   5e31608c -- Merge branch '198-find-all-training-definitions-return-only-logged-in-author' into 'master'
|\  
| * a1f5e309 -- Training definition repository modified, instead join fetch use inner join
|/  
*   b859f9e7 -- Merge branch '197-return-all-events-in-training-definition-in-specific-training-instance' into 'master'
|\  
| * fa1569fc -- Search events by TD id and TI id.
|/  
*   0fcbbc3a -- Merge branch '196-wrong-query-in-elasticsearch-testing-data-curl-es-6-7-sh-file' into 'master'
|\  
| * 65baa576 -- elasticsearch query for inserting template repair with JSON date format
|/  
*   b6cf65c9 -- Merge branch '195-emi-questions-in-assessment-can-be-asymetric' into 'master'
|\  
| * 40cd4fed -- Modified json schema and emi questions
|/  
*   7cd37794 -- Merge branch '193-implementing-elasticsearch-queries-into-java' into 'master'
|\  
| * 82405fcb -- Resolve "implementing elasticsearch queries into java"
|/  
*   243ea8dd -- Merge branch '174-rework-allocatesandboxes-method' into 'master'
|\  
| * 3ed5de77 -- Resolve "Rework allocateSandboxes method"
|/  
* 4f2500e7 -- Required for id queries on rest added.
*   5980671c -- Merge branch '194-change-permission-for-retrieving-associated-training-definitions-to-sandboxes' into 'master'
|\  
| * f5ed975c -- permission for retrieving training definitions by sandbox definitions changed from admin ro designer or admin.
|/  
*   5a6cfa6d -- Merge branch '190-set-custom-loggin-in-project' into 'master'
|\  
| * dab6c198 -- Resolve "Set custom loggin in project"
|/  
*   fc05d30c -- Merge branch '192-update-training-definition-save-null-user-in-beta-testing-group' into 'master'
|\  
| * 01f75c5b -- Null user in beta testing group when update training definition resolved.
|/  
*   b6893379 -- Merge branch '191-change-name-of-entity-view-group-to-betatesters-and-change-it-to-optional' into 'master'
|\  
| * 6596989a -- Resolve "Change name of entity View Group to BetaTesters and change it to optional"
|/  
*   d3fe5782 -- Merge branch '189-new-version-of-flyway-5-2-4' into 'master'
|\  
| * 28fe6350 -- Changed versions of flyway from 5.0.7 to 5.2.4
|/  
*   f1fd68d3 -- Merge branch '188-get-solution-taken-for-the-second-time-is-also-audited' into 'master'
|\  
| * f25754bf -- Resolve "Get solution taken for the second time is also audited"
|/  
*   8c37cc63 -- Merge branch '181-user-without-admin-rules-cannot-download-training-definition' into 'master'
|\  
| * 10999016 -- All designers can download training definition
|/  
*   4b94ae95 -- Merge branch '175-admin-role-is-needed-to-see-training-instances' into 'master'
|\  
| * c3523bc8 -- Organizers are allowed to see all training instances
|/  
*   bcd3bcdb -- Merge branch '177-state-of-training-run-is-not-stored-properly' into 'master'
|\  
| * e57d7b0b -- Solution taken twice decrease total score only once
|/  
*   9c93e2b9 -- Merge branch '187-resume-training-if-keyword-inserted-twice' into 'master'
|\  
| * 41c22e0e -- Accessed training run return correct level order/number of levels. Check training runs of participnat when access some training run.
|/  
* fe999941 -- Edit readme flyway command.
*   de52462b -- Merge branch '169-adjust-training-to-new-way-of-managing-roles-in-security-commons-and-user-and-group' into 'master'
|\  
| * 79903c8e -- Resolve "Adjust training to new way of managing roles in security commons and user and group"
|/  
*   a6183d7c -- Merge branch '168-implementation-of-trainingrun-integration-tests' into 'master'
|\  
| * 71a0921b -- Resolve "implementation of trainingRun integration tests"
|/  
*   6431554d -- Merge branch '186-authors-are-added-to-original-td-in-clone-method-original-authors-are-cloned-into-copy' into 'master'
|\  
| * 3ec5f641 -- problem with authors while cloning fixed
|/  
* a3104f46 -- Merge branch '184-add-pool-id-to-traininginstancedto' into 'master'
* ef2c71bd -- PoolId added to training instance dto