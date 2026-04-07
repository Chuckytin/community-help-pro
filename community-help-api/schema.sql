--
-- PostgreSQL database dump
--

\restrict gxQTkHfy7HHCtStxbLVUJbad6hbE1nW8f0YSaKusT1PKefUx7ndbxbLmcw3caRT

-- Dumped from database version 18.1 (Debian 18.1-1.pgdg13+2)
-- Dumped by pg_dump version 18.1 (Debian 18.1-1.pgdg13+2)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: tiger; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA tiger;


--
-- Name: topology; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA topology;


--
-- Name: SCHEMA topology; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA topology IS 'PostGIS Topology schema';


--
-- Name: fuzzystrmatch; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS fuzzystrmatch WITH SCHEMA public;


--
-- Name: EXTENSION fuzzystrmatch; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION fuzzystrmatch IS 'determine similarities and distance between strings';


--
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry and geography spatial types and functions';


--
-- Name: postgis_tiger_geocoder; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis_tiger_geocoder WITH SCHEMA tiger;


--
-- Name: EXTENSION postgis_tiger_geocoder; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION postgis_tiger_geocoder IS 'PostGIS tiger geocoder and reverse geocoder';


--
-- Name: postgis_topology; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis_topology WITH SCHEMA topology;


--
-- Name: EXTENSION postgis_topology; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION postgis_topology IS 'PostGIS topology spatial types and functions';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: conversation_participants; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.conversation_participants (
    id uuid NOT NULL,
    joined_at timestamp(6) without time zone NOT NULL,
    last_read_at timestamp(6) without time zone,
    conversation_id uuid NOT NULL,
    user_id uuid NOT NULL
);


--
-- Name: conversations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.conversations (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    related_entity_id uuid NOT NULL,
    type character varying(255) NOT NULL,
    CONSTRAINT conversations_type_check CHECK (((type)::text = ANY ((ARRAY['DONATION'::character varying, 'HELP_REQUEST'::character varying])::text[])))
);


--
-- Name: donations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.donations (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    location public.geography(Point,4326),
    active boolean NOT NULL,
    cancel_reason character varying(255),
    completed_at timestamp(6) without time zone,
    confirmed_at timestamp(6) without time zone,
    description text,
    donation_type character varying(255) NOT NULL,
    expiry_date timestamp(6) without time zone,
    food_type character varying(255),
    picked_up_at timestamp(6) without time zone,
    quantity integer NOT NULL,
    reserved_at timestamp(6) without time zone,
    status character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    unit character varying(30),
    donor_id uuid NOT NULL,
    volunteer_id uuid,
    CONSTRAINT donations_donation_type_check CHECK (((donation_type)::text = ANY ((ARRAY['FOOD'::character varying, 'CLOTHING'::character varying, 'HYGIENE'::character varying, 'TOYS'::character varying, 'FURNITURE'::character varying, 'ELECTRONICS'::character varying, 'MEDICAL_SUPPLIES'::character varying, 'BOOKS'::character varying, 'STATIONERY'::character varying, 'VEHICLES'::character varying, 'COMMUNITY_EQUIPMENT'::character varying, 'OTHER'::character varying])::text[]))),
    CONSTRAINT donations_food_type_check CHECK (((food_type)::text = ANY ((ARRAY['FRUIT'::character varying, 'VEGETABLE'::character varying, 'MEAT'::character varying, 'DAIRY'::character varying, 'COOKED'::character varying, 'OTHER'::character varying])::text[]))),
    CONSTRAINT donations_status_check CHECK (((status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'RESERVED'::character varying, 'CONFIRMED'::character varying, 'PICKED_UP'::character varying, 'COMPLETED'::character varying, 'EXPIRED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: help_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.help_requests (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    location public.geography(Point,4326),
    accepted_at timestamp(6) without time zone,
    active boolean NOT NULL,
    cancel_reason character varying(255),
    completed_at timestamp(6) without time zone,
    deadline timestamp(6) without time zone,
    description text,
    status character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    requester_id uuid NOT NULL,
    volunteer_id uuid,
    CONSTRAINT help_requests_status_check CHECK (((status)::text = ANY ((ARRAY['OPEN'::character varying, 'ACCEPTED'::character varying, 'COMPLETED'::character varying, 'EXPIRED'::character varying, 'CANCELLED'::character varying])::text[]))),
    CONSTRAINT help_requests_type_check CHECK (((type)::text = ANY ((ARRAY['FOOD'::character varying, 'TRANSPORT'::character varying, 'COMPANIONSHIP'::character varying, 'MEDICAL'::character varying, 'EDUCATION'::character varying, 'PET_CARE'::character varying, 'BABY_CARE'::character varying, 'COMMUNITY_EVENTS'::character varying, 'EMERGENCY'::character varying, 'SHOPPING'::character varying, 'OTHER'::character varying])::text[])))
);


--
-- Name: messages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages (
    id uuid NOT NULL,
    content text NOT NULL,
    deleted boolean NOT NULL,
    deleted_at timestamp(6) without time zone,
    sent_at timestamp(6) without time zone NOT NULL,
    conversation_id uuid NOT NULL,
    sender_id uuid NOT NULL
);


--
-- Name: otp_codes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.otp_codes (
    id bigint NOT NULL,
    code character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    type character varying(255) NOT NULL,
    used boolean NOT NULL,
    CONSTRAINT otp_codes_type_check CHECK (((type)::text = ANY ((ARRAY['VERIFY_EMAIL'::character varying, 'RESET_PASSWORD'::character varying])::text[])))
);


--
-- Name: otp_codes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.otp_codes ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.otp_codes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: pending_notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pending_notifications (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    entity_id uuid NOT NULL,
    entity_title character varying(255) NOT NULL,
    entity_type character varying(255) NOT NULL,
    sent boolean NOT NULL,
    volunteer_email character varying(255) NOT NULL,
    volunteer_id uuid NOT NULL,
    volunteer_name character varying(255) NOT NULL
);


--
-- Name: proposal_matching_state; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.proposal_matching_state (
    entity_id uuid NOT NULL,
    current_radius_meters integer NOT NULL,
    entity_type character varying(255),
    last_retry_at timestamp(6) without time zone,
    retry_count integer NOT NULL,
    version bigint,
    CONSTRAINT proposal_matching_state_entity_type_check CHECK (((entity_type)::text = ANY ((ARRAY['DONATION'::character varying, 'HELP_REQUEST'::character varying])::text[])))
);


--
-- Name: proposals; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.proposals (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    active boolean NOT NULL,
    cancel_reason character varying(255),
    responded_at timestamp(6) without time zone,
    score double precision,
    status character varying(255) NOT NULL,
    target_entity_id uuid NOT NULL,
    type character varying(255) NOT NULL,
    version bigint NOT NULL,
    volunteer_id uuid NOT NULL,
    CONSTRAINT proposals_cancel_reason_check CHECK (((cancel_reason)::text = ANY ((ARRAY['VOLUNTEER_UNAVAILABLE'::character varying, 'OTHER_PROPOSAL_ACCEPTED'::character varying, 'SYSTEM_EXPIRED'::character varying])::text[]))),
    CONSTRAINT proposals_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'EXPIRED'::character varying, 'CANCELLED'::character varying])::text[]))),
    CONSTRAINT proposals_type_check CHECK (((type)::text = ANY ((ARRAY['DONATION'::character varying, 'HELP_REQUEST'::character varying])::text[])))
);


--
-- Name: reviews; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reviews (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    comment text,
    rating integer NOT NULL,
    author_id uuid NOT NULL,
    donation_id uuid,
    help_request_id uuid,
    target_id uuid NOT NULL,
    CONSTRAINT reviews_rating_check CHECK (((rating <= 5) AND (rating >= 1)))
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    location public.geography(Point,4326),
    active boolean NOT NULL,
    deleted_at timestamp(6) without time zone,
    email character varying(255) NOT NULL,
    email_verified boolean NOT NULL,
    name character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    rating real,
    role character varying(255) NOT NULL,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['USER'::character varying, 'ADMIN'::character varying])::text[])))
);


--
-- Name: volunteer_skills; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.volunteer_skills (
    volunteer_id uuid NOT NULL,
    skills character varying(255),
    CONSTRAINT volunteer_skills_skills_check CHECK (((skills)::text = ANY ((ARRAY['TRANSPORT'::character varying, 'FOOD_HANDLING'::character varying, 'MEDICAL_ASSISTANCE'::character varying, 'ELDERLY_CARE'::character varying, 'SHOPPING'::character varying, 'COMMUNICATION'::character varying, 'COMPANIONSHIP'::character varying, 'PET_CARE'::character varying, 'BABY_CARE'::character varying, 'TECH'::character varying, 'HEAVY_LIFTING'::character varying, 'PATIENT'::character varying, 'LANGUAGE'::character varying, 'EVENT_PLANNING'::character varying, 'GARDENING'::character varying, 'PHYSICAL_LABOR'::character varying, 'LOGISTICS'::character varying, 'DRIVING'::character varying, 'FIRST_AID'::character varying, 'CRISIS_MANAGEMENT'::character varying, 'PSYCHOLOGICAL_SUPPORT'::character varying, 'ADMINISTRATIVE'::character varying, 'TEACHING'::character varying, 'CHILD_CARE'::character varying, 'ELDERLY_SUPPORT'::character varying, 'DISABILITY_SUPPORT'::character varying, 'EMERGENCY_RESPONSE'::character varying, 'TRANSLATION'::character varying, 'LEGAL_ADVICE'::character varying, 'FINANCIAL_ADVICE'::character varying, 'SOCIAL_MEDIA'::character varying, 'PHOTOGRAPHY'::character varying, 'WRITING'::character varying, 'CARPENTRY'::character varying, 'ELECTRICIAN'::character varying, 'PLUMBING'::character varying, 'PAINTING'::character varying, 'CLEANING'::character varying, 'COOKING'::character varying, 'CATERING'::character varying])::text[])))
);


--
-- Name: volunteers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.volunteers (
    user_id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone,
    available boolean NOT NULL,
    email_notifications_enabled boolean NOT NULL,
    radius_km double precision,
    transport_mode character varying(255),
    CONSTRAINT volunteers_transport_mode_check CHECK (((transport_mode)::text = ANY ((ARRAY['FOOT_WALKING'::character varying, 'DRIVING_CAR'::character varying, 'CYCLING_REGULAR'::character varying])::text[])))
);


--
-- Name: conversation_participants conversation_participants_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversation_participants
    ADD CONSTRAINT conversation_participants_pkey PRIMARY KEY (id);


--
-- Name: conversations conversations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT conversations_pkey PRIMARY KEY (id);


--
-- Name: donations donations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.donations
    ADD CONSTRAINT donations_pkey PRIMARY KEY (id);


--
-- Name: help_requests help_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.help_requests
    ADD CONSTRAINT help_requests_pkey PRIMARY KEY (id);


--
-- Name: messages messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_pkey PRIMARY KEY (id);


--
-- Name: otp_codes otp_codes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.otp_codes
    ADD CONSTRAINT otp_codes_pkey PRIMARY KEY (id);


--
-- Name: pending_notifications pending_notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pending_notifications
    ADD CONSTRAINT pending_notifications_pkey PRIMARY KEY (id);


--
-- Name: proposal_matching_state proposal_matching_state_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.proposal_matching_state
    ADD CONSTRAINT proposal_matching_state_pkey PRIMARY KEY (entity_id);


--
-- Name: proposals proposals_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.proposals
    ADD CONSTRAINT proposals_pkey PRIMARY KEY (id);


--
-- Name: reviews reviews_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_pkey PRIMARY KEY (id);


--
-- Name: reviews uk3bqtymbnvqio5ueecxs5egah3; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT uk3bqtymbnvqio5ueecxs5egah3 UNIQUE (author_id, donation_id);


--
-- Name: users uk6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: conversation_participants uk9sr5qudr0ccvvi0q4idhmpgt5; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversation_participants
    ADD CONSTRAINT uk9sr5qudr0ccvvi0q4idhmpgt5 UNIQUE (conversation_id, user_id);


--
-- Name: proposals uk_proposal_volunteer_target; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.proposals
    ADD CONSTRAINT uk_proposal_volunteer_target UNIQUE (volunteer_id, target_entity_id, type);


--
-- Name: reviews ukjacy6pof1vl7m7bds56fntivk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT ukjacy6pof1vl7m7bds56fntivk UNIQUE (author_id, help_request_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: volunteers volunteers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.volunteers
    ADD CONSTRAINT volunteers_pkey PRIMARY KEY (user_id);


--
-- Name: idx_donation_donor; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_donation_donor ON public.donations USING btree (donor_id);


--
-- Name: idx_donation_donor_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_donation_donor_status ON public.donations USING btree (donor_id, status);


--
-- Name: idx_donation_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_donation_status ON public.donations USING btree (status);


--
-- Name: idx_donation_volunteer; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_donation_volunteer ON public.donations USING btree (volunteer_id);


--
-- Name: idx_help_requester; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_help_requester ON public.help_requests USING btree (requester_id);


--
-- Name: idx_help_requester_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_help_requester_status ON public.help_requests USING btree (requester_id, status);


--
-- Name: idx_help_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_help_status ON public.help_requests USING btree (status);


--
-- Name: idx_help_volunteer; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_help_volunteer ON public.help_requests USING btree (volunteer_id);


--
-- Name: idx_proposal_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_proposal_status ON public.proposals USING btree (status);


--
-- Name: idx_proposal_target; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_proposal_target ON public.proposals USING btree (target_entity_id);


--
-- Name: idx_proposal_volunteer; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_proposal_volunteer ON public.proposals USING btree (volunteer_id);


--
-- Name: idx_review_author; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_review_author ON public.reviews USING btree (author_id);


--
-- Name: idx_review_donation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_review_donation ON public.reviews USING btree (donation_id);


--
-- Name: idx_review_help_request; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_review_help_request ON public.reviews USING btree (help_request_id);


--
-- Name: idx_review_target; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_review_target ON public.reviews USING btree (target_id);


--
-- Name: idx_users_location; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_location ON public.users USING btree (location);


--
-- Name: messages fk4ui4nnwntodh6wjvck53dbk9m; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fk4ui4nnwntodh6wjvck53dbk9m FOREIGN KEY (sender_id) REFERENCES public.users(id);


--
-- Name: reviews fk7nxdogqtq60bypxrtk2rhvq56; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT fk7nxdogqtq60bypxrtk2rhvq56 FOREIGN KEY (target_id) REFERENCES public.users(id);


--
-- Name: volunteers fk7w082tdu6mqd1a1doc0o6ju4e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.volunteers
    ADD CONSTRAINT fk7w082tdu6mqd1a1doc0o6ju4e FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: conversation_participants fk84npv3fo2vwl7ut63im0p417q; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversation_participants
    ADD CONSTRAINT fk84npv3fo2vwl7ut63im0p417q FOREIGN KEY (conversation_id) REFERENCES public.conversations(id);


--
-- Name: proposals fkcirqu8qtmipyvgntbe3dipa4t; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.proposals
    ADD CONSTRAINT fkcirqu8qtmipyvgntbe3dipa4t FOREIGN KEY (volunteer_id) REFERENCES public.volunteers(user_id);


--
-- Name: reviews fkcv2c9pltgo9ilvsvauu5xolom; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT fkcv2c9pltgo9ilvsvauu5xolom FOREIGN KEY (help_request_id) REFERENCES public.help_requests(id);


--
-- Name: help_requests fkdu2tr7klawsa7igw7ix0f9sxx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.help_requests
    ADD CONSTRAINT fkdu2tr7klawsa7igw7ix0f9sxx FOREIGN KEY (volunteer_id) REFERENCES public.volunteers(user_id);


--
-- Name: reviews fkf8djywvvq9ruwr8qnao5twk5e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT fkf8djywvvq9ruwr8qnao5twk5e FOREIGN KEY (donation_id) REFERENCES public.donations(id);


--
-- Name: donations fki3hwj0kvd7sle2hqy80y42whk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.donations
    ADD CONSTRAINT fki3hwj0kvd7sle2hqy80y42whk FOREIGN KEY (volunteer_id) REFERENCES public.volunteers(user_id);


--
-- Name: conversation_participants fkjukjgq6uinvvk4307y8u9lixu; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversation_participants
    ADD CONSTRAINT fkjukjgq6uinvvk4307y8u9lixu FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: volunteer_skills fknxb6p0d64p91vxsvkj9uclfm7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.volunteer_skills
    ADD CONSTRAINT fknxb6p0d64p91vxsvkj9uclfm7 FOREIGN KEY (volunteer_id) REFERENCES public.volunteers(user_id);


--
-- Name: donations fkp8lwp38vg4a0v2y69d2krn562; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.donations
    ADD CONSTRAINT fkp8lwp38vg4a0v2y69d2krn562 FOREIGN KEY (donor_id) REFERENCES public.users(id);


--
-- Name: help_requests fkq3mknlt4g85cyyvgh3tjclbo1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.help_requests
    ADD CONSTRAINT fkq3mknlt4g85cyyvgh3tjclbo1 FOREIGN KEY (requester_id) REFERENCES public.users(id);


--
-- Name: reviews fkse5kx11600wtv0jh9jobvrdpi; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT fkse5kx11600wtv0jh9jobvrdpi FOREIGN KEY (author_id) REFERENCES public.users(id);


--
-- Name: messages fkt492th6wsovh1nush5yl5jj8e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fkt492th6wsovh1nush5yl5jj8e FOREIGN KEY (conversation_id) REFERENCES public.conversations(id);


--
-- PostgreSQL database dump complete
--

\unrestrict gxQTkHfy7HHCtStxbLVUJbad6hbE1nW8f0YSaKusT1PKefUx7ndbxbLmcw3caRT

