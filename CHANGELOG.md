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
* bffcdd36 -- Merge branch '446-update-docker-with-supervisord' into 'master'
* 6b40d7f5 -- Resolve "Update docker with supervisord"