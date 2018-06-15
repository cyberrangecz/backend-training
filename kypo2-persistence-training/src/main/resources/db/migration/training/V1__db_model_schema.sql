--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.5
-- Dumped by pg_dump version 9.6.5

-- Started on 2018-06-15 12:44:23

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2204 (class 1262 OID 17098)
-- Name: training; Type: DATABASE; Schema: -; Owner: postgres
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
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
-- TOC entry 2205 (class 0 OID 0)
-- Dependencies: 1
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 186 (class 1259 OID 22201)
-- Name: abstract_level; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE abstract_level (
    id bigint NOT NULL,
    max_score integer NOT NULL,
    next_level bigint NOT NULL,
    "order" integer NOT NULL,
    post_hook oid,
    pre_hook oid,
    title character varying(255) NOT NULL,
    training_definition_id bigint
);


ALTER TABLE abstract_level OWNER TO postgres;

--
-- TOC entry 185 (class 1259 OID 22199)
-- Name: abstract_level_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE abstract_level_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE abstract_level_id_seq OWNER TO postgres;

--
-- TOC entry 2206 (class 0 OID 0)
-- Dependencies: 185
-- Name: abstract_level_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE abstract_level_id_seq OWNED BY abstract_level.id;


--
-- TOC entry 187 (class 1259 OID 22207)
-- Name: assessment_level; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE assessment_level (
    assessment_type character varying(128) NOT NULL,
    instructions character varying(255) NOT NULL,
    questions jsonb NOT NULL,
    id bigint NOT NULL
);


ALTER TABLE assessment_level OWNER TO postgres;

--
-- TOC entry 188 (class 1259 OID 22215)
-- Name: game_level; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE game_level (
    attachments bytea,
    content oid NOT NULL,
    estimated_duration integer,
    flag character varying(255) NOT NULL,
    incorrect_flag_penalty integer NOT NULL,
    solution oid NOT NULL,
    solution_penalty integer NOT NULL,
    id bigint NOT NULL
);


ALTER TABLE game_level OWNER TO postgres;

--
-- TOC entry 190 (class 1259 OID 22225)
-- Name: hint; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE hint (
    id bigint NOT NULL,
    content oid NOT NULL,
    hint_penalty integer NOT NULL,
    title character varying(255) NOT NULL,
    game_level_id bigint
);


ALTER TABLE hint OWNER TO postgres;

--
-- TOC entry 189 (class 1259 OID 22223)
-- Name: hint_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE hint_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE hint_id_seq OWNER TO postgres;

--
-- TOC entry 2207 (class 0 OID 0)
-- Dependencies: 189
-- Name: hint_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE hint_id_seq OWNED BY hint.id;


--
-- TOC entry 191 (class 1259 OID 22231)
-- Name: info_level; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE info_level (
    content oid NOT NULL,
    id bigint NOT NULL
);


ALTER TABLE info_level OWNER TO postgres;

--
-- TOC entry 193 (class 1259 OID 22238)
-- Name: training_definition; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE training_definition (
    id bigint NOT NULL,
    description character varying(255),
    outcomes bytea,
    prerequisities bytea,
    state character varying(128) NOT NULL,
    title character varying(255) NOT NULL
);


ALTER TABLE training_definition OWNER TO postgres;

--
-- TOC entry 192 (class 1259 OID 22236)
-- Name: training_definition_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE training_definition_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE training_definition_id_seq OWNER TO postgres;

--
-- TOC entry 2208 (class 0 OID 0)
-- Dependencies: 192
-- Name: training_definition_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE training_definition_id_seq OWNED BY training_definition.id;


--
-- TOC entry 195 (class 1259 OID 22249)
-- Name: training_instance; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE training_instance (
    id bigint NOT NULL,
    end_time timestamp without time zone,
    keyword character varying(255) NOT NULL,
    pool_size integer NOT NULL,
    start_time timestamp without time zone NOT NULL,
    title character varying(255) NOT NULL,
    training_definition_id bigint
);


ALTER TABLE training_instance OWNER TO postgres;

--
-- TOC entry 194 (class 1259 OID 22247)
-- Name: training_instance_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE training_instance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE training_instance_id_seq OWNER TO postgres;

--
-- TOC entry 2209 (class 0 OID 0)
-- Dependencies: 194
-- Name: training_instance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE training_instance_id_seq OWNED BY training_instance.id;


--
-- TOC entry 197 (class 1259 OID 22260)
-- Name: training_run; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE training_run (
    id bigint NOT NULL,
    end_time timestamp without time zone NOT NULL,
    event_log_reference character varying(255),
    start_time timestamp without time zone NOT NULL,
    state character varying(128) NOT NULL,
    current_level_id bigint,
    training_instance_id bigint
);


ALTER TABLE training_run OWNER TO postgres;

--
-- TOC entry 196 (class 1259 OID 22258)
-- Name: training_run_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE training_run_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE training_run_id_seq OWNER TO postgres;

--
-- TOC entry 2210 (class 0 OID 0)
-- Dependencies: 196
-- Name: training_run_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE training_run_id_seq OWNED BY training_run.id;


--
-- TOC entry 2041 (class 2604 OID 22204)
-- Name: abstract_level id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY abstract_level ALTER COLUMN id SET DEFAULT nextval('abstract_level_id_seq'::regclass);


--
-- TOC entry 2042 (class 2604 OID 22228)
-- Name: hint id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY hint ALTER COLUMN id SET DEFAULT nextval('hint_id_seq'::regclass);


--
-- TOC entry 2043 (class 2604 OID 22241)
-- Name: training_definition id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_definition ALTER COLUMN id SET DEFAULT nextval('training_definition_id_seq'::regclass);


--
-- TOC entry 2044 (class 2604 OID 22252)
-- Name: training_instance id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_instance ALTER COLUMN id SET DEFAULT nextval('training_instance_id_seq'::regclass);


--
-- TOC entry 2045 (class 2604 OID 22263)
-- Name: training_run id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_run ALTER COLUMN id SET DEFAULT nextval('training_run_id_seq'::regclass);


--
-- TOC entry 2188 (class 0 OID 22201)
-- Dependencies: 186
-- Data for Name: abstract_level; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY abstract_level (id, max_score, next_level, "order", post_hook, pre_hook, title, training_definition_id) FROM stdin;
\.


--
-- TOC entry 2211 (class 0 OID 0)
-- Dependencies: 185
-- Name: abstract_level_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('abstract_level_id_seq', 1, false);


--
-- TOC entry 2189 (class 0 OID 22207)
-- Dependencies: 187
-- Data for Name: assessment_level; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY assessment_level (assessment_type, instructions, questions, id) FROM stdin;
\.


--
-- TOC entry 2190 (class 0 OID 22215)
-- Dependencies: 188
-- Data for Name: game_level; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY game_level (attachments, content, estimated_duration, flag, incorrect_flag_penalty, solution, solution_penalty, id) FROM stdin;
\.


--
-- TOC entry 2192 (class 0 OID 22225)
-- Dependencies: 190
-- Data for Name: hint; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY hint (id, content, hint_penalty, title, game_level_id) FROM stdin;
\.


--
-- TOC entry 2212 (class 0 OID 0)
-- Dependencies: 189
-- Name: hint_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('hint_id_seq', 1, false);


--
-- TOC entry 2193 (class 0 OID 22231)
-- Dependencies: 191
-- Data for Name: info_level; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY info_level (content, id) FROM stdin;
\.


--
-- TOC entry 2195 (class 0 OID 22238)
-- Dependencies: 193
-- Data for Name: training_definition; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY training_definition (id, description, outcomes, prerequisities, state, title) FROM stdin;
\.


--
-- TOC entry 2213 (class 0 OID 0)
-- Dependencies: 192
-- Name: training_definition_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('training_definition_id_seq', 1, false);


--
-- TOC entry 2197 (class 0 OID 22249)
-- Dependencies: 195
-- Data for Name: training_instance; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY training_instance (id, end_time, keyword, pool_size, start_time, title, training_definition_id) FROM stdin;
\.


--
-- TOC entry 2214 (class 0 OID 0)
-- Dependencies: 194
-- Name: training_instance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('training_instance_id_seq', 1, false);


--
-- TOC entry 2199 (class 0 OID 22260)
-- Dependencies: 197
-- Data for Name: training_run; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY training_run (id, end_time, event_log_reference, start_time, state, current_level_id, training_instance_id) FROM stdin;
\.


--
-- TOC entry 2215 (class 0 OID 0)
-- Dependencies: 196
-- Name: training_run_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('training_run_id_seq', 1, false);


--
-- TOC entry 2047 (class 2606 OID 22206)
-- Name: abstract_level abstract_level_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY abstract_level
    ADD CONSTRAINT abstract_level_pkey PRIMARY KEY (id);


--
-- TOC entry 2049 (class 2606 OID 22214)
-- Name: assessment_level assessment_level_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY assessment_level
    ADD CONSTRAINT assessment_level_pkey PRIMARY KEY (id);


--
-- TOC entry 2051 (class 2606 OID 22222)
-- Name: game_level game_level_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY game_level
    ADD CONSTRAINT game_level_pkey PRIMARY KEY (id);


--
-- TOC entry 2053 (class 2606 OID 22230)
-- Name: hint hint_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY hint
    ADD CONSTRAINT hint_pkey PRIMARY KEY (id);


--
-- TOC entry 2055 (class 2606 OID 22235)
-- Name: info_level info_level_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY info_level
    ADD CONSTRAINT info_level_pkey PRIMARY KEY (id);


--
-- TOC entry 2057 (class 2606 OID 22246)
-- Name: training_definition training_definition_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_definition
    ADD CONSTRAINT training_definition_pkey PRIMARY KEY (id);


--
-- TOC entry 2059 (class 2606 OID 22257)
-- Name: training_instance training_instance_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_instance
    ADD CONSTRAINT training_instance_pkey PRIMARY KEY (id);


--
-- TOC entry 2061 (class 2606 OID 22265)
-- Name: training_run training_run_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_run
    ADD CONSTRAINT training_run_pkey PRIMARY KEY (id);


--
-- TOC entry 2067 (class 2606 OID 22291)
-- Name: training_instance fk28s41pqjyqwrni7thb54tidru; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_instance
    ADD CONSTRAINT fk28s41pqjyqwrni7thb54tidru FOREIGN KEY (training_definition_id) REFERENCES training_definition(id);


--
-- TOC entry 2068 (class 2606 OID 22296)
-- Name: training_run fk4hccsnb42pdpd4wkyh4ati16v; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_run
    ADD CONSTRAINT fk4hccsnb42pdpd4wkyh4ati16v FOREIGN KEY (current_level_id) REFERENCES abstract_level(id);


--
-- TOC entry 2066 (class 2606 OID 22286)
-- Name: info_level fk765u7yobjffxdntpsveghsfs5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY info_level
    ADD CONSTRAINT fk765u7yobjffxdntpsveghsfs5 FOREIGN KEY (id) REFERENCES abstract_level(id);


--
-- TOC entry 2069 (class 2606 OID 22301)
-- Name: training_run fk7vajehsxurugwfg363f4ppb0s; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY training_run
    ADD CONSTRAINT fk7vajehsxurugwfg363f4ppb0s FOREIGN KEY (training_instance_id) REFERENCES training_instance(id);


--
-- TOC entry 2062 (class 2606 OID 22266)
-- Name: abstract_level fk9ij1mj0eebjxpiwcwbpu2lg9a; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY abstract_level
    ADD CONSTRAINT fk9ij1mj0eebjxpiwcwbpu2lg9a FOREIGN KEY (training_definition_id) REFERENCES training_definition(id);


--
-- TOC entry 2063 (class 2606 OID 22271)
-- Name: assessment_level fka6plseuaj8fuj8hcybstxc15g; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY assessment_level
    ADD CONSTRAINT fka6plseuaj8fuj8hcybstxc15g FOREIGN KEY (id) REFERENCES abstract_level(id);


--
-- TOC entry 2065 (class 2606 OID 22281)
-- Name: hint fkikeediy8uqdf22egpfmdaboor; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY hint
    ADD CONSTRAINT fkikeediy8uqdf22egpfmdaboor FOREIGN KEY (game_level_id) REFERENCES game_level(id);


--
-- TOC entry 2064 (class 2606 OID 22276)
-- Name: game_level fkiw8w6qjmivl6nibogektgfn0h; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY game_level
    ADD CONSTRAINT fkiw8w6qjmivl6nibogektgfn0h FOREIGN KEY (id) REFERENCES abstract_level(id);


-- Completed on 2018-06-15 12:44:23

--
-- PostgreSQL database dump complete
--

