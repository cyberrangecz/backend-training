--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.5
-- Dumped by pg_dump version 9.6.5

-- Started on 2018-06-21 17:03:05

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 1 (class 3079 OID 12387)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 2304 (class 0 OID 0)
-- Dependencies: 1
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 186 (class 1259 OID 24261)
-- Name: abstract_level; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE abstract_level (
    id bigint NOT NULL,
    max_score integer NOT NULL,
    next_level bigint,
    title character varying(255) NOT NULL,
    post_hook_id bigint,
    pre_hook_id bigint
);

ALTER TABLE abstract_level OWNER TO postgres;

--
-- TOC entry 185 (class 1259 OID 24259)
-- Name: abstract_level_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE abstract_level_id_seq
    START WITH 7
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE abstract_level_id_seq OWNER TO postgres;

--
-- TOC entry 2305 (class 0 OID 0)
-- Dependencies: 185
-- Name: abstract_level_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE abstract_level_id_seq OWNED BY abstract_level.id;


--
-- TOC entry 187 (class 1259 OID 24267)
-- Name: assessment_level; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE assessment_level (
    id bigint NOT NULL,
    assessment_type character varying(128) NOT NULL,
    instructions text NOT NULL,
    questions text NOT NULL
);
ALTER TABLE assessment_level OWNER TO postgres;

CREATE TABLE participant_ref (
    id bigint NOT NULL,
    participant_ref_login character varying(255) NOT NULL
);

ALTER TABLE participant_ref OWNER TO postgres;

CREATE SEQUENCE participant_ref_id_seq
    START WITH 3
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE participant_ref_id_seq OWNER TO postgres;

--
-- TOC entry 2306 (class 0 OID 0)
-- Dependencies: 188
-- Name: author_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE participant_ref_id_seq OWNED BY participant_ref.id;

ALTER TABLE ONLY participant_ref
    ADD CONSTRAINT participant_ref_pkey PRIMARY KEY (id);
--
-- TOC entry 189 (class 1259 OID 24277)
-- Name: author_ref; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE author_ref (
    id bigint NOT NULL,
    author_ref_login character varying(255) NOT NULL
);


ALTER TABLE author_ref OWNER TO postgres;

--
-- TOC entry 188 (class 1259 OID 24275)
-- Name: author_ref_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE author_ref_id_seq
    START WITH 4
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE author_ref_id_seq OWNER TO postgres;

--
-- TOC entry 2306 (class 0 OID 0)
-- Dependencies: 188
-- Name: author_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE author_ref_id_seq OWNED BY author_ref.id;


--
-- TOC entry 190 (class 1259 OID 24283)
-- Name: game_level; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE game_level (
    id bigint NOT NULL,
    attachments bytea,
    content text NOT NULL,
    estimated_duration integer,
    flag character varying(255) NOT NULL,
    solution text NOT NULL,
    solution_penalized boolean NOT NULL,
    incorrect_flag_limit integer
);


ALTER TABLE game_level OWNER TO postgres;

--
-- TOC entry 192 (class 1259 OID 24293)
-- Name: hint; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE hint (
    id bigint NOT NULL,
    content text NOT NULL,
    hint_penalty integer NOT NULL,
    title character varying(255) NOT NULL,
    game_level_id bigint
);


ALTER TABLE hint OWNER TO postgres;

--
-- TOC entry 191 (class 1259 OID 24291)
-- Name: hint_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE hint_id_seq
    START WITH 3
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE hint_id_seq OWNER TO postgres;

--
-- TOC entry 2307 (class 0 OID 0)
-- Dependencies: 191
-- Name: hint_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE hint_id_seq OWNED BY hint.id;


--
-- TOC entry 193 (class 1259 OID 24302)
-- Name: info_level; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE info_level (
    id bigint NOT NULL,
    content text NOT NULL
);


ALTER TABLE info_level OWNER TO postgres;

--
-- TOC entry 195 (class 1259 OID 24312)
-- Name: post_hook; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE post_hook (
    id bigint NOT NULL
);


ALTER TABLE post_hook OWNER TO postgres;

--
-- TOC entry 194 (class 1259 OID 24310)
-- Name: post_hook_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE post_hook_id_seq
    START WITH 7
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE post_hook_id_seq OWNER TO postgres;

--
-- TOC entry 2308 (class 0 OID 0)
-- Dependencies: 194
-- Name: post_hook_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE post_hook_id_seq OWNED BY post_hook.id;


--
-- TOC entry 197 (class 1259 OID 24320)
-- Name: pre_hook; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE pre_hook (
    id bigint NOT NULL
);


ALTER TABLE pre_hook OWNER TO postgres;

--
-- TOC entry 196 (class 1259 OID 24318)
-- Name: pre_hook_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE pre_hook_id_seq
    START WITH 7
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE pre_hook_id_seq OWNER TO postgres;

--
-- TOC entry 2309 (class 0 OID 0)
-- Dependencies: 196
-- Name: pre_hook_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE pre_hook_id_seq OWNED BY pre_hook.id;


--
-- TOC entry 199 (class 1259 OID 24328)
-- Name: sandbox_definition_ref; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE sandbox_definition_ref (
    id bigint NOT NULL,
    sandbox_definition_ref bigint
);


ALTER TABLE sandbox_definition_ref OWNER TO postgres;

--
-- TOC entry 198 (class 1259 OID 24326)
-- Name: sandbox_definition_ref_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE sandbox_definition_ref_id_seq
    START WITH 3
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE sandbox_definition_ref_id_seq OWNER TO postgres;

--
-- TOC entry 2310 (class 0 OID 0)
-- Dependencies: 198
-- Name: sandbox_definition_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE sandbox_definition_ref_id_seq OWNED BY sandbox_definition_ref.id;


--
-- TOC entry 201 (class 1259 OID 24336)
-- Name: sandbox_instance_ref; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE sandbox_instance_ref (
    id bigint NOT NULL,
    sandbox_instance_ref bigint,
    training_instance_id bigint
);


ALTER TABLE sandbox_instance_ref OWNER TO postgres;

--
-- TOC entry 200 (class 1259 OID 24334)
-- Name: sandbox_instance_ref_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE sandbox_instance_ref_id_seq
    START WITH 4
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE sandbox_instance_ref_id_seq OWNER TO postgres;

--
-- TOC entry 2311 (class 0 OID 0)
-- Dependencies: 200
-- Name: sandbox_instance_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE sandbox_instance_ref_id_seq OWNED BY sandbox_instance_ref.id;


--
-- TOC entry 203 (class 1259 OID 24344)
-- Name: training_definition; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE training_definition (
    id bigint NOT NULL,
    description character varying(255),
    outcomes bytea,
    prerequisities bytea,
    state character varying(128) NOT NULL,
    title character varying(255) NOT NULL,
    sand_box_definition_ref_id bigint,
    starting_level bigint,
    show_stepper_bar boolean
);


ALTER TABLE training_definition OWNER TO postgres;

--
-- TOC entry 204 (class 1259 OID 24353)
-- Name: training_definition_author_ref; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE training_definition_author_ref (
    training_definition_id bigint NOT NULL,
    author_ref_id bigint NOT NULL
);


ALTER TABLE training_definition_author_ref OWNER TO postgres;

--
-- TOC entry 202 (class 1259 OID 24342)
-- Name: training_definition_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE training_definition_id_seq
    START WITH 3
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE training_definition_id_seq OWNER TO postgres;

--
-- TOC entry 2312 (class 0 OID 0)
-- Dependencies: 202
-- Name: training_definition_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE training_definition_id_seq OWNED BY training_definition.id;


--
-- TOC entry 206 (class 1259 OID 24360)
-- Name: training_instance; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE training_instance (
    id bigint NOT NULL,
    end_time timestamp without time zone NOT NULL,
    password character varying(255),
    pool_size integer NOT NULL,
    start_time timestamp without time zone NOT NULL,
    title character varying(255) NOT NULL,
    training_definition_id bigint
);


ALTER TABLE training_instance OWNER TO postgres;

--
-- TOC entry 205 (class 1259 OID 24358)
-- Name: training_instance_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE training_instance_id_seq
    START WITH 4
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE training_instance_id_seq OWNER TO postgres;

--
-- TOC entry 2313 (class 0 OID 0)
-- Dependencies: 205
-- Name: training_instance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE training_instance_id_seq OWNED BY training_instance.id;


--
-- TOC entry 207 (class 1259 OID 24369)
-- Name: training_instance_organizers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE training_instance_organizers (
    training_instance_id bigint NOT NULL,
    organizers_id bigint NOT NULL
);


ALTER TABLE training_instance_organizers OWNER TO postgres;

--
-- TOC entry 208 (class 1259 OID 24374)
-- Name: training_instance_sandbox_instance_ref; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE training_instance_sandbox_instance_ref (
    training_instance_id bigint NOT NULL,
    sandbox_instance_ref_id bigint NOT NULL
);


ALTER TABLE training_instance_sandbox_instance_ref OWNER TO postgres;

--
-- TOC entry 210 (class 1259 OID 24381)
-- Name: training_run; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE training_definition_sandbox_definition_ref (
    training_definition_id bigint NOT NULL,
    sandbox_definition_ref_id bigint NOT NULL
);

ALTER TABLE training_definition_sandbox_definition_ref OWNER TO postgres;

CREATE TABLE training_run (
    id bigint NOT NULL,
    end_time timestamp without time zone NOT NULL,
    start_time timestamp without time zone NOT NULL,
    state character varying(128) NOT NULL,
    current_level_id bigint,
    sandbox_instance_ref_id bigint,
    training_instance_id bigint,
    participant_ref_id bigint,
    solution_taken boolean NOT NULL,
    event_log_reference character varying(255),
    incorrect_flag_count integer,
    assessment_responses text,
    total_score integer,
    current_score integer,
    level_answered boolean NOT NULL
);


ALTER TABLE training_run OWNER TO postgres;

CREATE SEQUENCE training_run_id_seq
    START WITH 3
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE training_run_id_seq OWNER TO postgres;

--
-- TOC entry 2314 (class 0 OID 0)
-- Dependencies: 209
-- Name: training_run_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE training_run_id_seq OWNED BY training_run.id;


--
-- TOC entry 212 (class 1259 OID 24389)
-- Name: user_ref; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE user_ref (
    id bigint NOT NULL,
    user_ref_login character varying(255) NOT NULL
);


ALTER TABLE user_ref OWNER TO postgres;

--
-- TOC entry 211 (class 1259 OID 24387)
-- Name: user_ref_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE user_ref_id_seq
    START WITH 3
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

 CREATE TABLE password (
  id bigint NOT NULL PRIMARY KEY,
  password character varying(255)
);


CREATE SEQUENCE password_id_seq
    START WITH 4
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE password OWNER TO postgres;

ALTER TABLE ONLY password ALTER COLUMN id SET DEFAULT nextval('password_id_seq'::regclass);

ALTER TABLE user_ref_id_seq OWNER TO postgres;

--
-- TOC entry 2315 (class 0 OID 0)
-- Dependencies: 211
-- Name: user_ref_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE user_ref_id_seq OWNED BY user_ref.id;


--
-- TOC entry 2091 (class 2604 OID 24264)
-- Name: abstract_level id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY abstract_level ALTER COLUMN id SET DEFAULT nextval('abstract_level_id_seq'::regclass);


--
-- TOC entry 2092 (class 2604 OID 24280)
-- Name: author_ref id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY author_ref ALTER COLUMN id SET DEFAULT nextval('author_ref_id_seq'::regclass);


--
-- TOC entry 2093 (class 2604 OID 24296)
-- Name: hint id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY hint ALTER COLUMN id SET DEFAULT nextval('hint_id_seq'::regclass);


--
-- TOC entry 2094 (class 2604 OID 24315)
-- Name: post_hook id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY post_hook ALTER COLUMN id SET DEFAULT nextval('post_hook_id_seq'::regclass);


--
-- TOC entry 2095 (class 2604 OID 24323)
-- Name: pre_hook id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY pre_hook ALTER COLUMN id SET DEFAULT nextval('pre_hook_id_seq'::regclass);


--
-- TOC entry 2096 (class 2604 OID 24331)
-- Name: sandbox_definition_ref id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY sandbox_definition_ref ALTER COLUMN id SET DEFAULT nextval('sandbox_definition_ref_id_seq'::regclass);


--
-- TOC entry 2097 (class 2604 OID 24339)
-- Name: sandbox_instance_ref id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY sandbox_instance_ref ALTER COLUMN id SET DEFAULT nextval('sandbox_instance_ref_id_seq'::regclass);


--
-- TOC entry 2098 (class 2604 OID 24347)
-- Name: training_definition id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_definition ALTER COLUMN id SET DEFAULT nextval('training_definition_id_seq'::regclass);


--
-- TOC entry 2099 (class 2604 OID 24363)
-- Name: training_instance id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_instance ALTER COLUMN id SET DEFAULT nextval('training_instance_id_seq'::regclass);


--
-- TOC entry 2100 (class 2604 OID 24384)
-- Name: training_run id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_run ALTER COLUMN id SET DEFAULT nextval('training_run_id_seq'::regclass);


--
-- TOC entry 2101 (class 2604 OID 24392)
-- Name: user_ref id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY user_ref ALTER COLUMN id SET DEFAULT nextval('user_ref_id_seq'::regclass);


--
-- TOC entry 2272 (class 0 OID 24261)
-- Dependencies: 186
-- Data for Name: abstract_level; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY abstract_level (id, max_score, next_level, title, post_hook_id, pre_hook_id) FROM stdin;
\.


--
-- TOC entry 2316 (class 0 OID 0)
-- Dependencies: 185
-- Name: abstract_level_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('abstract_level_id_seq', 1, false);


--
-- TOC entry 2273 (class 0 OID 24267)
-- Dependencies: 187
-- Data for Name: assessment_level; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY assessment_level (assessment_type, instructions, questions, id) FROM stdin;
\.


--
-- TOC entry 2275 (class 0 OID 24277)
-- Dependencies: 189
-- Data for Name: author_ref; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY author_ref (id, author_ref_login) FROM stdin;
\.


--
-- TOC entry 2317 (class 0 OID 0)
-- Dependencies: 188
-- Name: author_ref_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('author_ref_id_seq', 1, false);


--
-- TOC entry 2276 (class 0 OID 24283)
-- Dependencies: 190
-- Data for Name: game_level; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY game_level (attachments, content, estimated_duration, flag, incorrect_flag_limit, solution, solution_penalized, id) FROM stdin;
\.


--
-- TOC entry 2278 (class 0 OID 24293)
-- Dependencies: 192
-- Data for Name: hint; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY hint (id, content, hint_penalty, title, game_level_id) FROM stdin;
\.


--
-- TOC entry 2318 (class 0 OID 0)
-- Dependencies: 191
-- Name: hint_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('hint_id_seq', 1, false);


--
-- TOC entry 2279 (class 0 OID 24302)
-- Dependencies: 193
-- Data for Name: info_level; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY info_level (content, id) FROM stdin;
\.


--
-- TOC entry 2281 (class 0 OID 24312)
-- Dependencies: 195
-- Data for Name: post_hook; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY post_hook (id) FROM stdin;
\.


--
-- TOC entry 2319 (class 0 OID 0)
-- Dependencies: 194
-- Name: post_hook_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('post_hook_id_seq', 1, false);


--
-- TOC entry 2283 (class 0 OID 24320)
-- Dependencies: 197
-- Data for Name: pre_hook; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY pre_hook (id) FROM stdin;
\.


--
-- TOC entry 2320 (class 0 OID 0)
-- Dependencies: 196
-- Name: pre_hook_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('pre_hook_id_seq', 1, false);


--
-- TOC entry 2285 (class 0 OID 24328)
-- Dependencies: 199
-- Data for Name: sandbox_definition_ref; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY sandbox_definition_ref (id, sandbox_definition_ref) FROM stdin;
\.


--
-- TOC entry 2321 (class 0 OID 0)
-- Dependencies: 198
-- Name: sandbox_definition_ref_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('sandbox_definition_ref_id_seq', 1, false);


--
-- TOC entry 2287 (class 0 OID 24336)
-- Dependencies: 201
-- Data for Name: sandbox_instance_ref; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY sandbox_instance_ref (id, sandbox_instance_ref) FROM stdin;
\.


--
-- TOC entry 2322 (class 0 OID 0)
-- Dependencies: 200
-- Name: sandbox_instance_ref_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('sandbox_instance_ref_id_seq', 1, false);


--
-- TOC entry 2289 (class 0 OID 24344)
-- Dependencies: 203
-- Data for Name: training_definition; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY training_definition (id, description, outcomes, prerequisities, state, title, sand_box_definition_ref_id, show_stepper_bar) FROM stdin;
\.


--
-- TOC entry 2290 (class 0 OID 24353)
-- Dependencies: 204
-- Data for Name: training_definition_author_ref; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY training_definition_author_ref (training_definition_id, author_ref_id) FROM stdin;
\.


--
-- TOC entry 2323 (class 0 OID 0)
-- Dependencies: 202
-- Name: training_definition_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('training_definition_id_seq', 1, false);


--
-- TOC entry 2292 (class 0 OID 24360)
-- Dependencies: 206
-- Data for Name: training_instance; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY training_instance (id, end_time, password, pool_size, start_time, title, training_definition_id) FROM stdin;
\.


--
-- TOC entry 2324 (class 0 OID 0)
-- Dependencies: 205
-- Name: training_instance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('training_instance_id_seq', 1, false);


--
-- TOC entry 2293 (class 0 OID 24369)
-- Dependencies: 207
-- Data for Name: training_instance_organizers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY training_instance_organizers (training_instance_id, organizers_id) FROM stdin;
\.


--
-- TOC entry 2294 (class 0 OID 24374)
-- Dependencies: 208
-- Data for Name: training_instance_sandbox_instance_ref; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY training_instance_sandbox_instance_ref (training_instance_id, sandbox_instance_ref_id) FROM stdin;
\.

COPY training_definition_sandbox_definition_ref (training_definition_id, sandbox_definition_ref_id) FROM stdin;
\.
--
-- TOC entry 2296 (class 0 OID 24381)
-- Dependencies: 210
-- Data for Name: training_run; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY training_run (id, end_time, event_log_reference, start_time, state, current_level_id, sandbox_instance_ref_id, training_instance_id) FROM stdin;
\.


--
-- TOC entry 2325 (class 0 OID 0)
-- Dependencies: 209
-- Name: training_run_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('training_run_id_seq', 1, false);


--
-- TOC entry 2298 (class 0 OID 24389)
-- Dependencies: 212
-- Data for Name: user_ref; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY user_ref (id, user_ref_login) FROM stdin;
\.


--
-- TOC entry 2326 (class 0 OID 0)
-- Dependencies: 211
-- Name: user_ref_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('user_ref_id_seq', 1, false);


--
-- TOC entry 2103 (class 2606 OID 24266)
-- Name: abstract_level abstract_level_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY abstract_level
    ADD CONSTRAINT abstract_level_pkey PRIMARY KEY (id);


--
-- TOC entry 2105 (class 2606 OID 24274)
-- Name: assessment_level assessment_level_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY assessment_level
    ADD CONSTRAINT assessment_level_pkey PRIMARY KEY (id);


--
-- TOC entry 2107 (class 2606 OID 24282)
-- Name: author_ref author_ref_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY author_ref
    ADD CONSTRAINT author_ref_pkey PRIMARY KEY (id);


--
-- TOC entry 2109 (class 2606 OID 24290)
-- Name: game_level game_level_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY game_level
    ADD CONSTRAINT game_level_pkey PRIMARY KEY (id);


--
-- TOC entry 2111 (class 2606 OID 24301)
-- Name: hint hint_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY hint
    ADD CONSTRAINT hint_pkey PRIMARY KEY (id);


--
-- TOC entry 2113 (class 2606 OID 24309)
-- Name: info_level info_level_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY info_level
    ADD CONSTRAINT info_level_pkey PRIMARY KEY (id);


--
-- TOC entry 2115 (class 2606 OID 24317)
-- Name: post_hook post_hook_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY post_hook
    ADD CONSTRAINT post_hook_pkey PRIMARY KEY (id);


--
-- TOC entry 2117 (class 2606 OID 24325)
-- Name: pre_hook pre_hook_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY pre_hook
    ADD CONSTRAINT pre_hook_pkey PRIMARY KEY (id);


--
-- TOC entry 2119 (class 2606 OID 24333)
-- Name: sandbox_definition_ref sandbox_definition_ref_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY sandbox_definition_ref
    ADD CONSTRAINT sandbox_definition_ref_pkey PRIMARY KEY (id);


--
-- TOC entry 2121 (class 2606 OID 24341)
-- Name: sandbox_instance_ref sandbox_instance_ref_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY sandbox_instance_ref
    ADD CONSTRAINT sandbox_instance_ref_pkey PRIMARY KEY (id);


--
-- TOC entry 2125 (class 2606 OID 24357)
-- Name: training_definition_author_ref training_definition_author_ref_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_definition_author_ref
    ADD CONSTRAINT training_definition_author_ref_pkey PRIMARY KEY (training_definition_id, author_ref_id);


--
-- TOC entry 2123 (class 2606 OID 24352)
-- Name: training_definition training_definition_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_definition
    ADD CONSTRAINT training_definition_pkey PRIMARY KEY (id);


--
-- TOC entry 2129 (class 2606 OID 24373)
-- Name: training_instance_organizers training_instance_organizers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_instance_organizers
    ADD CONSTRAINT training_instance_organizers_pkey PRIMARY KEY (training_instance_id, organizers_id);


--
-- TOC entry 2127 (class 2606 OID 24368)
-- Name: training_instance training_instance_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_instance
    ADD CONSTRAINT training_instance_pkey PRIMARY KEY (id);


--
-- TOC entry 2131 (class 2606 OID 24378)
-- Name: training_instance_sandbox_instance_ref training_instance_sandbox_instance_ref_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_instance_sandbox_instance_ref
    ADD CONSTRAINT training_instance_sandbox_instance_ref_pkey PRIMARY KEY (training_instance_id, sandbox_instance_ref_id);

ALTER TABLE ONLY training_definition_sandbox_definition_ref
    ADD CONSTRAINT training_definition_sandbox_definition_ref_pkey PRIMARY KEY (training_definition_id, sandbox_definition_ref_id);

--
-- TOC entry 2133 (class 2606 OID 24386)
-- Name: training_run training_run_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_run
    ADD CONSTRAINT training_run_pkey PRIMARY KEY (id);


--
-- TOC entry 2135 (class 2606 OID 24394)
-- Name: user_ref user_ref_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY user_ref
    ADD CONSTRAINT user_ref_pkey PRIMARY KEY (id);

--
-- TOC entry 2146 (class 2606 OID 24445)
-- Name: training_instance fk28s41pqjyqwrni7thb54tidru; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_instance
    ADD CONSTRAINT fk28s41pqjyqwrni7thb54tidru FOREIGN KEY (training_definition_id) REFERENCES training_definition(id);


--
-- TOC entry 2152 (class 2606 OID 24475)
-- Name: training_run fk6yn4e9w78a454vegxipn3cmvf; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_run
    ADD CONSTRAINT fk6yn4e9w78a454vegxipn3cmvf FOREIGN KEY (sandbox_instance_ref_id) REFERENCES sandbox_instance_ref(id);


--
-- TOC entry 2144 (class 2606 OID 24435)
-- Name: training_definition_author_ref fk76ifve9d4sreenamcmrwsh9tm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_definition_author_ref
    ADD CONSTRAINT fk76ifve9d4sreenamcmrwsh9tm FOREIGN KEY (author_ref_id) REFERENCES author_ref(id);


--
-- TOC entry 2139 (class 2606 OID 24410)
-- Name: assessment_level fk7jxec7ef838ovnrnfw73kh95; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY assessment_level
    ADD CONSTRAINT fk7jxec7ef838ovnrnfw73kh95 FOREIGN KEY (id) REFERENCES abstract_level(id);


--
-- TOC entry 2153 (class 2606 OID 24480)
-- Name: training_run fk7vajehsxurugwfg363f4ppb0s; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_run
    ADD CONSTRAINT fk7vajehsxurugwfg363f4ppb0s FOREIGN KEY (training_instance_id) REFERENCES training_instance(id);


--
-- TOC entry 2145 (class 2606 OID 24440)
-- Name: training_definition_author_ref fk83b30979dnp5kade6m4600h7n; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_definition_author_ref
    ADD CONSTRAINT fk83b30979dnp5kade6m4600h7n FOREIGN KEY (training_definition_id) REFERENCES training_definition(id);


--
-- TOC entry 2149 (class 2606 OID 24460)
-- Name: training_instance_sandbox_instance_ref fk93ieg5ncp2jgxdn4b6ufty1wg; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_instance_sandbox_instance_ref
    ADD CONSTRAINT fk93ieg5ncp2jgxdn4b6ufty1wg FOREIGN KEY (sandbox_instance_ref_id) REFERENCES sandbox_instance_ref(id);

ALTER TABLE ONLY training_definition_sandbox_definition_ref
    ADD CONSTRAINT FK_sandbox_definition_ref FOREIGN KEY (sandbox_definition_ref_id) REFERENCES sandbox_definition_ref(id);

--
-- TOC entry 2142 (class 2606 OID 24425)
-- Name: info_level fka9ssogmfce6duhtlm8chrqcc4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY info_level
    ADD CONSTRAINT fka9ssogmfce6duhtlm8chrqcc4 FOREIGN KEY (id) REFERENCES abstract_level(id);


--
-- TOC entry 2150 (class 2606 OID 24465)
-- Name: training_instance_sandbox_instance_ref fkayuh2k5x6e1dwssc29p3jl58y; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_instance_sandbox_instance_ref
    ADD CONSTRAINT fkayuh2k5x6e1dwssc29p3jl58y FOREIGN KEY (training_instance_id) REFERENCES training_instance(id);


ALTER TABLE ONLY training_definition_sandbox_definition_ref
    ADD CONSTRAINT FK_training_definition FOREIGN KEY (training_definition_id) REFERENCES training_definition(id);

--
-- TOC entry 2151 (class 2606 OID 24470)
-- Name: training_run fkddva9h2olm0h0aj9veb6jfe9r; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_run
    ADD CONSTRAINT fkddva9h2olm0h0aj9veb6jfe9r FOREIGN KEY (current_level_id) REFERENCES abstract_level(id);


--
-- TOC entry 2148 (class 2606 OID 24455)
-- Name: training_instance_organizers fke4qmx0nnbqxvg66wwt0si91vr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_instance_organizers
    ADD CONSTRAINT fke4qmx0nnbqxvg66wwt0si91vr FOREIGN KEY (training_instance_id) REFERENCES training_instance(id);


--
-- TOC entry 2136 (class 2606 OID 24395)
-- Name: abstract_level fkfur32sh4k57g53x45w3d9mrv6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY abstract_level
    ADD CONSTRAINT fkfur32sh4k57g53x45w3d9mrv6 FOREIGN KEY (post_hook_id) REFERENCES post_hook(id);


--
-- TOC entry 2137 (class 2606 OID 24400)
-- Name: abstract_level fkh97onob6w74379lvjq8jjiy1b; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY abstract_level
    ADD CONSTRAINT fkh97onob6w74379lvjq8jjiy1b FOREIGN KEY (pre_hook_id) REFERENCES pre_hook(id);


--
-- TOC entry 2141 (class 2606 OID 24420)
-- Name: hint fkikeediy8uqdf22egpfmdaboor; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY hint
    ADD CONSTRAINT fkikeediy8uqdf22egpfmdaboor FOREIGN KEY (game_level_id) REFERENCES game_level(id);


--
-- TOC entry 2143 (class 2606 OID 24430)
-- Name: training_definition fklpslmg909yvgsihw6ribpcjee; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_definition
    ADD CONSTRAINT fklpslmg909yvgsihw6ribpcjee FOREIGN KEY (sand_box_definition_ref_id) REFERENCES sandbox_definition_ref(id);


--
-- TOC entry 2147 (class 2606 OID 24450)
-- Name: training_instance_organizers fkofnq5p3x5u0o0c15a1oj9ckpx; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_instance_organizers
    ADD CONSTRAINT fkofnq5p3x5u0o0c15a1oj9ckpx FOREIGN KEY (organizers_id) REFERENCES user_ref(id);


--
-- TOC entry 2140 (class 2606 OID 24415)
-- Name: game_level fkrg7pvp6aqm4gxshunqq77noma; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY game_level
    ADD CONSTRAINT fkrg7pvp6aqm4gxshunqq77noma FOREIGN KEY (id) REFERENCES abstract_level(id);


-- Completed on 2018-06-21 17:03:06

--
-- PostgreSQL database dump complete
--


