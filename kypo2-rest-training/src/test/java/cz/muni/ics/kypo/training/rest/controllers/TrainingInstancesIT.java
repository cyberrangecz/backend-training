//    @Test
//    public void findAllTrainingInstancesAsAdmin() throws Exception {
//        trainingInstanceRepository.save(notConcludedTrainingInstance);
//        trainingInstanceRepository.save(futureTrainingInstance);
//        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ADMINISTRATOR.name()));
//
//        MockHttpServletResponse result = mvc.perform(get("/training-instances"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andReturn().getResponse();
//        PageResultResource<TrainingInstanceDTO> trainingInstancesPage = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), new TypeReference<PageResultResource<TrainingInstanceDTO>>() {
//        });
//        assertTrue(trainingInstancesPage.getContent().contains(trainingInstanceMapper.mapToDTO(notConcludedTrainingInstance)));
//        assertTrue(trainingInstancesPage.getContent().contains(trainingInstanceMapper.mapToDTO(futureTrainingInstance)));
//    }
//
//    @Test
//    public void findTrainingInstanceById() throws Exception {
//        sandboxInstanceRef1.setTrainingInstance(futureTrainingInstance);
//        sandboxInstanceRef2.setTrainingInstance(futureTrainingInstance);
//        futureTrainingInstance.setSandboxInstanceRefs(Set.of(sandboxInstanceRef1, sandboxInstanceRef2));
//        trainingInstanceRepository.save(futureTrainingInstance);
//
//        trainingRun1.setSandboxInstanceRef(sandboxInstanceRef1);
//        trainingRunRepository.save(trainingRun1);
//        MockHttpServletResponse result = mvc.perform(get("/training-instances/{id}", futureTrainingInstance.getId()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andReturn().getResponse();
//
//        TrainingInstanceDTO expectedInstanceDTO = trainingInstanceMapper.mapToDTO(futureTrainingInstance);
//        expectedInstanceDTO.setSandboxesWithTrainingRun(List.of(sandboxInstanceRef1.getId()));
//        TrainingInstanceDTO responseInstanceDTO = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), TrainingInstanceDTO.class);
//        assertEquals(expectedInstanceDTO, responseInstanceDTO);
//    }
//
//    @Test
//    public void findTrainingInstanceByIdNotFound() throws Exception {
//        Exception ex = mvc.perform(get("/training-instances/{id}", 100L))
//                .andExpect(status().isNotFound())
//                .andReturn().getResolvedException();
//
//        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
//        assertTrue(ex.getMessage().contains("Training instance with id: 100 not found"));
//    }
//
//    @Test
//    public void createTrainingInstance() throws Exception {
//        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
//                willReturn(new ResponseEntity<List<UserInfoDTO>>(new ArrayList<>(Collections.singletonList(userInfoDTO)), HttpStatus.OK));
//
//        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
//        MockHttpServletResponse result = mvc.perform(post("/training-instances").content(convertObjectToJsonBytes(trainingInstanceCreateDTO))
//                .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andReturn().getResponse();
//
//        Optional<TrainingInstance> newInstance = trainingInstanceRepository.findById(1L);
//        assertTrue(newInstance.isPresent());
//        TrainingInstanceDTO newInstanceDTO = trainingInstanceMapper.mapToDTO(newInstance.get());
//
//        assertTrue(newInstanceDTO.getOrganizers().stream().anyMatch(userRefDTO -> userRefDTO.getUserRefLogin().equals("556978@muni.cz")));
//        assertEquals(newInstanceDTO, mapper.readValue(convertJsonBytesToString(result.getContentAsString()), TrainingInstanceDTO.class));
//    }
//
//    @Test
//    public void createInvalidTrainingInstance() throws Exception {
//        Exception ex = mvc.perform(post("/training-instances").content(convertObjectToJsonBytes(new TrainingInstanceCreateDTO()))
//                .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(status().isBadRequest())
//                .andReturn().getResolvedException();
//
//        assertTrue(Objects.requireNonNull(ex).getMessage().contains("Validation failed for argument"));
//        assertEquals(ex.getClass(), MethodArgumentNotValidException.class);
//    }
//
//    @Test
//    public void updateTrainingInstance() throws Exception {
//        trainingInstanceRepository.save(futureTrainingInstance);
//        trainingInstanceUpdateDTO.setAccessToken(futureTrainingInstance.getAccessToken());
//        trainingInstanceUpdateDTO.setId(futureTrainingInstance.getId());
//        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
//                willReturn(new ResponseEntity<List<UserInfoDTO>>(new ArrayList<>(Collections.singletonList(userInfoDTO)), HttpStatus.OK));
//        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
//        MockHttpServletResponse result = mvc.perform(put("/training-instances")
//                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
//                .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andReturn().getResponse();
//
//        Optional<TrainingInstance> newInstance = trainingInstanceRepository.findById(futureTrainingInstance.getId());
//        assertTrue(newInstance.isPresent());
//
//        assertEquals(newInstance.get().getAccessToken(), convertJsonBytesToString(result.getContentAsString()));
//    }
//
//    @Test
//    public void updateTrainingInstanceTrainingDefinitionNotFoundd() throws Exception {
//        trainingInstanceUpdateDTO.setId(100L);
//        trainingInstanceUpdateDTO.setAccessToken("preff");
//        trainingInstanceUpdateDTO.setTrainingDefinitionId(100L);
//        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
//        Exception ex = mvc.perform(put("/training-instances")
//                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
//                .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(status().isNotFound())
//                .andReturn().getResolvedException();
//
//        assertEquals(ResourceNotFoundException.class, Objects.requireNonNull(ex).getClass());
//        assertEquals("Training definition with id: 100 not found.", ex.getCause().getCause().getMessage());
//    }
//
//    @Test
//    public void updateTrainingInstanceNotFound() throws Exception {
//        trainingInstanceUpdateDTO.setAccessToken("someToken");
//        trainingInstanceUpdateDTO.setId(500L);
//        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
//                willReturn(new ResponseEntity<List<UserInfoDTO>>(new ArrayList<>(Collections.singletonList(userInfoDTO)), HttpStatus.OK));
//        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
//        Exception ex = mvc.perform(put("/training-instances")
//                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
//                .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(status().isNotFound())
//                .andReturn().getResolvedException();
//
//        assertEquals(ResourceNotFoundException.class, Objects.requireNonNull(ex).getClass());
//        assertEquals("Training instance with id: 500, not found.", ex.getCause().getCause().getMessage());
//    }
//
//    @Test
//    public void updateTrainingInstanceWrongStartTime() throws Exception {
//        trainingInstanceRepository.save(futureTrainingInstance);
//        trainingInstanceUpdateDTO.setAccessToken("someToken");
//        trainingInstanceUpdateDTO.setId(futureTrainingInstance.getId());
//        trainingInstanceUpdateDTO.setEndTime(LocalDateTime.now().plusHours(2));
//        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
//                willReturn(new ResponseEntity<List<UserInfoDTO>>(new ArrayList<>(Collections.singletonList(userInfoDTO)), HttpStatus.OK));
//        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
//        Exception ex = mvc.perform(put("/training-instances")
//                .content(convertObjectToJsonBytes(trainingInstanceUpdateDTO))
//                .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(status().isConflict())
//                .andReturn().getResolvedException();
//
//        assertEquals(ConflictException.class, Objects.requireNonNull(ex).getClass());
//        assertEquals("End time must be later than start time.", ex.getCause().getCause().getMessage());
//    }
////TODO Boris
////    @Test
////    public void deleteTrainingInstance() throws Exception {
////        TrainingInstance tI = trainingInstanceRepository.save(futureTrainingInstance);
////        mvc.perform(delete("/training-instances/{id}", tI.getId()))
////                .andExpect(status().isOk());
////        Optional<TrainingInstance> optTI = trainingInstanceRepository.findById(tI.getId());
////        assertFalse(optTI.isPresent());
////    }
////
////    @Test
////    public void deleteFinishedTrainingInstanceWithTrainingRuns() throws Exception {
////        trainingInstanceRepository.save(finishedTrainingInstance);
////        trainingRun1.setTrainingInstance(finishedTrainingInstance);
////        trainingRun1.setSandboxInstanceRef(sandboxInstanceRef1);
////        sandboxInstanceRef1.setTrainingInstance(finishedTrainingInstance);
////        trainingRunRepository.save(trainingRun1);
////        Exception ex = mvc.perform(delete("/training-instances/{id}", finishedTrainingInstance.getId()))
////                .andExpect(status().isConflict())
////                .andReturn().getResolvedException();
////        assertEquals(ConflictException.class, Objects.requireNonNull(ex).getClass());
////        assertTrue(ex.getMessage().contains("Finished training instance with already assigned training runs cannot be deleted."));
////    }
////
////    @Test
////    public void deleteTrainingInstanceWithSandboxes() throws Exception {
////        sandboxInstanceRef1.setTrainingInstance(finishedTrainingInstance);
////        finishedTrainingInstance.setPoolId(5L);
////        finishedTrainingInstance.setPoolSize(3);
////        finishedTrainingInstance.setSandboxInstanceRefs(Set.of(sandboxInstanceRef1));
////        trainingInstanceRepository.save(finishedTrainingInstance);
////        Exception ex = mvc.perform(delete("/training-instances/{id}", finishedTrainingInstance.getId()))
////                .andExpect(status().isConflict())
////                .andReturn().getResolvedException();
////        assertEquals(ConflictException.class, Objects.requireNonNull(ex).getClass());
////        assertTrue(ex.getMessage().contains("Cannot delete training instance because it contains some sandboxes. Please delete sandboxes and try again."));
////    }
////
////    @Test
////    public void allocateSandboxes() throws Exception {
////        futureTrainingInstance.setPoolId(3L);
////        futureTrainingInstance.setPoolSize(2);
////        trainingInstanceRepository.save(futureTrainingInstance);
////        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
////                .willReturn(new ResponseEntity<List<SandboxInfo>>(new ArrayList<>(), HttpStatus.OK));
////        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
////                willReturn(new ResponseEntity<List<SandboxInfo>>(new ArrayList<>(List.of(sandboxInfo1, sandboxInfo2)), HttpStatus.OK));
////        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
////        mvc.perform(post("/training-instances/{instanceId}/sandbox-instances", futureTrainingInstance.getId())
////                .contentType(MediaType.APPLICATION_JSON_VALUE))
////                .andExpect(status().isAccepted());
////    }
//
//    @Test
//    public void allocateSandboxesWithoutCreatedPool() throws Exception {
//        trainingInstanceRepository.save(futureTrainingInstance);
//        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
//        Exception exception = mvc.perform(post("/training-instances/{instanceId}/sandbox-instances", futureTrainingInstance.getId())
//                .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(status().isConflict())
//                .andReturn().getResolvedException();
//        assertEquals(ConflictException.class, Objects.requireNonNull(exception).getClass());
//        assertEquals("Pool for sandboxes is not created yet. Please create pool before allocating sandboxes.", exception.getCause().getCause().getMessage());
//    }
//    //TODO Boris
////    @Test
////    public void allocateSandboxesWithFullPool() throws Exception {
////        futureTrainingInstance.setPoolId(3L);
////        futureTrainingInstance.setPoolSize(1);
////        SandboxInfo sandboxInfo = new SandboxInfo();
////        sandboxInfo.setId(sandboxInstanceRef1.getSandboxInstanceRef());
////        sandboxInstanceRef1.setTrainingInstance(futureTrainingInstance);
////        futureTrainingInstance.setSandboxInstanceRefs(new HashSet<>(Set.of(sandboxInstanceRef1)));
////
////        trainingInstanceRepository.save(futureTrainingInstance);
////        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
////                .willReturn(new ResponseEntity<List<SandboxInfo>>(new ArrayList<>(List.of(sandboxInfo)), HttpStatus.OK));
////
////        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
////        Exception exception = mvc.perform(post("/training-instances/{instanceId}/sandbox-instances", futureTrainingInstance.getId())
////                .contentType(MediaType.APPLICATION_JSON_VALUE))
////                .andExpect(status().isConflict())
////                .andReturn().getResolvedException();
////        assertEquals(ConflictException.class, exception.getClass());
////        assertEquals("Pool of sandboxes of training instance with id: 1 is full.", exception.getCause().getCause().getMessage());
////    }
//
//    @Test
//    public void createPoolForSandboxes() throws Exception {
//        futureTrainingInstance.setPoolSize(5);
//        trainingInstanceRepository.save(futureTrainingInstance);
//        sandboxPoolInfo.setMaxSize(5L);
//        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(SandboxPoolInfo.class))).
//                willReturn(new ResponseEntity<SandboxPoolInfo>(sandboxPoolInfo, HttpStatus.OK));
//        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
//        MockHttpServletResponse result = mvc.perform(post("/training-instances/{instanceId}/pools", futureTrainingInstance.getId())
//                .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(status().isCreated())
//                .andReturn().getResponse();
//        assertEquals(sandboxPoolInfo.getId().toString(), result.getContentAsString());
//    }
//
//    @Test
//    public void createPoolInInstanceWithAlreadyCreatedPool() throws Exception {
//        futureTrainingInstance.setPoolId(sandboxPoolInfo.getId());
//        futureTrainingInstance.setPoolSize(1);
//        trainingInstanceRepository.save(futureTrainingInstance);
//        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
//                willReturn(new ResponseEntity<SandboxPoolInfo>(sandboxPoolInfo, HttpStatus.OK));
//        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
//        MockHttpServletResponse result = mvc.perform(post("/training-instances/{instanceId}/pools", futureTrainingInstance.getId())
//                .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(status().isCreated())
//                .andReturn().getResponse();
//        assertEquals(sandboxPoolInfo.getId().toString(), result.getContentAsString());
//    }
//
//    @Test
//    public void findAllTrainingRunsByTrainingInstanceId() throws Exception {
//        futureTrainingInstance.addSandboxInstanceRef(trainingRun1.getSandboxInstanceRef());
//        futureTrainingInstance.addSandboxInstanceRef(trainingRun2.getSandboxInstanceRef());
//        trainingInstanceRepository.save(futureTrainingInstance);
//        trainingRunRepository.save(trainingRun1);
//        trainingRunRepository.save(trainingRun2);
//
//        MockHttpServletResponse result = mvc.perform(get("/training-instances/{instanceId}/training-runs", futureTrainingInstance.getId()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andReturn().getResponse();
//        PageResultResource<TrainingRunDTO> trainingRunsPage = mapper.readValue(convertJsonBytesToString(result.getContentAsString()), new TypeReference<PageResultResource<TrainingRunDTO>>() {
//        });
//        assertTrue(trainingRunsPage.getContent().contains(trainingRunMapper.mapToDTO(trainingRun1)));
//        assertTrue(trainingRunsPage.getContent().contains(trainingRunMapper.mapToDTO(trainingRun2)));
//    }
//
//    @Test
//    public void findAllTrainingRunsByTrainingInstanceIdWithNonexistentInstance() throws Exception {
//        Exception ex = mvc.perform(get("/training-instances/{instanceId}/training-runs", 100L))
//                .andExpect(status().isNotFound())
//                .andReturn().getResolvedException();
//        assertEquals(Objects.requireNonNull(ex).getClass(), ResourceNotFoundException.class);
//        assertTrue(ex.getMessage().contains("Training instance with id: 100 not found."));
//    }
//
//    @Test
//    public void deleteSandboxes() throws Exception {
//        futureTrainingInstance.setPoolId(3L);
//        futureTrainingInstance.setPoolSize(2);
//        futureTrainingInstance.addSandboxInstanceRef(sandboxInstanceRef1);
//        futureTrainingInstance.addSandboxInstanceRef(sandboxInstanceRef2);
//
//        trainingInstanceRepository.save(futureTrainingInstance);
//        given(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class))).
//                willReturn(new ResponseEntity<String>("", HttpStatus.OK));
//        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
//        mvc.perform(delete("/training-instances/{instanceId}/sandbox-instances", futureTrainingInstance.getId())
//                .param("sandboxIds", sandboxInstanceRef2.getSandboxInstanceRef().toString(), sandboxInstanceRef1.getSandboxInstanceRef().toString())
//                .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(status().isOk());
//        assertTrue(futureTrainingInstance.getSandboxInstanceRefs().isEmpty());
//    }
//
//    @Test
//    public void deleteNonExistentSandbox() throws Exception {
//        sandboxInstanceRef1.setTrainingInstance(futureTrainingInstance);
//        sandboxInstanceRef2.setTrainingInstance(futureTrainingInstance);
//        futureTrainingInstance.setPoolId(3L);
//        futureTrainingInstance.setPoolSize(3);
//        futureTrainingInstance.setSandboxInstanceRefs(new HashSet<>(Set.of(sandboxInstanceRef1, sandboxInstanceRef2)));
//
//        trainingInstanceRepository.save(futureTrainingInstance);
//        given(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class))).
//                willReturn(new ResponseEntity<String>("", HttpStatus.OK));
//        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
//        mvc.perform(delete("/training-instances/{instanceId}/sandbox-instances", futureTrainingInstance.getId())
//                .param("sandboxIds", sandboxInstanceRef1.getSandboxInstanceRef().toString(), "156")
//                .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(status().isOk());
//        assertEquals(1, futureTrainingInstance.getSandboxInstanceRefs().size());
//        assertTrue(futureTrainingInstance.getSandboxInstanceRefs().contains(sandboxInstanceRef2));
//    }
//
//    @Test
//    public void reallocateSandbox() throws Exception {
//        sandboxInstanceRef1.setTrainingInstance(futureTrainingInstance);
//        sandboxInstanceRef2.setTrainingInstance(futureTrainingInstance);
//        futureTrainingInstance.setPoolId(3L);
//        futureTrainingInstance.setPoolSize(10);
//        futureTrainingInstance.setSandboxInstanceRefs(new HashSet<>(Set.of(sandboxInstanceRef1, sandboxInstanceRef2)));
//
//        trainingInstanceRepository.save(futureTrainingInstance);
//        given(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class))).
//                willReturn(new ResponseEntity<String>("", HttpStatus.OK));
//        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
//                willReturn(new ResponseEntity<List<SandboxInfo>>(new ArrayList<>(List.of(sandboxInfo1)), HttpStatus.OK));
//        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
//        mvc.perform(post("/training-instances/{instanceId}/sandbox-instances/{sandboxId}", futureTrainingInstance.getId(), sandboxInstanceRef1.getSandboxInstanceRef())
//                .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(status().isAccepted());
//        assertTrue(futureTrainingInstance.getSandboxInstanceRefs().contains(sandboxInstanceRef2));
//        assertFalse(futureTrainingInstance.getSandboxInstanceRefs().contains(sandboxInstanceRef1));
//        assertTrue(futureTrainingInstance.getSandboxInstanceRefs().stream().anyMatch(sandboxInstanceRef ->
//                sandboxInstanceRef.getSandboxInstanceRef().equals(sandboxInfo1.getId())));
//    }
//
//    @Test
//    public void reallocateSandboxWithNoSpaceForNewSandbox() throws Exception {
//        sandboxInstanceRef1.setTrainingInstance(futureTrainingInstance);
//        sandboxInstanceRef2.setTrainingInstance(futureTrainingInstance);
//        futureTrainingInstance.setPoolId(3L);
//        futureTrainingInstance.setPoolSize(2);
//        futureTrainingInstance.setSandboxInstanceRefs(new HashSet<>(Set.of(sandboxInstanceRef1, sandboxInstanceRef2)));
//
//        trainingInstanceRepository.save(futureTrainingInstance);
//        given(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class))).
//                willReturn(new ResponseEntity<String>("", HttpStatus.OK));
//        given(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).
//                willReturn(new ResponseEntity<List<SandboxInfo>>(new ArrayList<>(List.of(sandboxInfo1)), HttpStatus.OK));
//        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
//        Exception exception = mvc.perform(post("/training-instances/{instanceId}/sandbox-instances/{sandboxId}", futureTrainingInstance.getId(), sandboxInstanceRef1.getSandboxInstanceRef())
//                .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(status().isConflict())
//                .andReturn().getResolvedException();
//        assertTrue(futureTrainingInstance.getSandboxInstanceRefs().contains(sandboxInstanceRef2));
//        assertFalse(futureTrainingInstance.getSandboxInstanceRefs().contains(sandboxInstanceRef1));
//        assertEquals("Sandbox cannot be reallocated because pool of training instance with id: " + futureTrainingInstance.getId() + " is full. " +
//                        "Given sandbox with id: " + sandboxInstanceRef1.getSandboxInstanceRef() + " is probably in the process of removing right now. Please wait and try allocate new sandbox later or contact administrator.",
//                Objects.requireNonNull(exception).getCause().getCause().getMessage());
//    }
//
//    @Test
//    public void reallocateNonExistentSandbox() throws Exception {
//        sandboxInstanceRef1.setTrainingInstance(futureTrainingInstance);
//        sandboxInstanceRef2.setTrainingInstance(futureTrainingInstance);
//        futureTrainingInstance.setPoolId(3L);
//        futureTrainingInstance.setPoolSize(2);
//        futureTrainingInstance.setSandboxInstanceRefs(new HashSet<>(Set.of(sandboxInstanceRef1, sandboxInstanceRef2)));
//
//        trainingInstanceRepository.save(futureTrainingInstance);
//        mockSpringSecurityContextForGet(List.of(RoleType.ROLE_TRAINING_ORGANIZER.name()));
//        Exception exception = mvc.perform(post("/training-instances/{instanceId}/sandbox-instances/{sandboxId}", futureTrainingInstance.getId(), 156)
//                .contentType(MediaType.APPLICATION_JSON_VALUE))
//                .andExpect(status().isNotFound())
//                .andReturn().getResolvedException();
//        assertEquals(ResourceNotFoundException.class, Objects.requireNonNull(exception).getClass());
//        assertEquals("Given sandbox with id: 156 is not in DB or is not assigned to given training instance.",
//                exception.getCause().getCause().getMessage());
//    }
//
//    private static String convertObjectToJsonBytes(Object object) throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
//        SimpleModule simpleModule = new SimpleModule("SimpleModule").addSerializer(new LocalDateTimeUTCSerializer());
//        mapper.registerModule(simpleModule);
//        return mapper.writeValueAsString(object);
//    }
//
//    private void mockSpringSecurityContextForGet(List<String> roles) {
//        List<GrantedAuthority> authorities = new ArrayList<>();
//        for (String role : roles) {
//            authorities.add(new SimpleGrantedAuthority(role));
//        }
//        JsonObject sub = new JsonObject();
//        sub.addProperty(AuthenticatedUserOIDCItems.SUB.getName(), "556978@muni.cz");
//        sub.addProperty(AuthenticatedUserOIDCItems.NAME.getName(), "Ing. Michael Johnson");
//        sub.addProperty(AuthenticatedUserOIDCItems.GIVEN_NAME.getName(), "Michael");
//        sub.addProperty(AuthenticatedUserOIDCItems.FAMILY_NAME.getName(), "Johnson");
//        Authentication authentication = Mockito.mock(Authentication.class);
//        OAuth2Authentication auth = Mockito.mock(OAuth2Authentication.class);
//        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
//        SecurityContextHolder.setContext(securityContext);
//        given(securityContext.getAuthentication()).willReturn(auth);
//        given(auth.getUserAuthentication()).willReturn(auth);
//        given(auth.getCredentials()).willReturn(sub);
//        given(auth.getAuthorities()).willReturn(authorities);
//        given(authentication.getDetails()).willReturn(auth);
//    }
//
//    private static String convertJsonBytesToString(String object) throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
//        return mapper.readValue(object, String.class);
//    }
//
//    private UserRef createUserRef(String login, String fullName, String givenName, String familyName, String iss, Long userRefId) {
//        UserRef userRef = new UserRef();
//        userRef.setUserRefLogin(login);
//        userRef.setUserRefFullName(fullName);
//        userRef.setUserRefGivenName(givenName);
//        userRef.setUserRefFamilyName(familyName);
//        userRef.setIss(iss);
//        userRef.setUserRefId(userRefId);
//        return userRef;
//    }
//
//}

