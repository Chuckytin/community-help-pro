-- TABLES

CREATE TABLE users (
                       id uuid PRIMARY KEY,
                       created_at timestamp NOT NULL,
                       updated_at timestamp,
                       location geography(Point,4326),
                       active boolean NOT NULL,
                       deleted_at timestamp,
                       email varchar(255) NOT NULL UNIQUE,
                       email_verified boolean NOT NULL,
                       name varchar(255) NOT NULL,
                       password_hash varchar(255) NOT NULL,
                       rating real,
                       role varchar(255) NOT NULL CHECK (role IN ('USER','ADMIN'))
);

CREATE TABLE volunteers (
                            user_id uuid PRIMARY KEY,
                            created_at timestamp NOT NULL,
                            updated_at timestamp,
                            available boolean NOT NULL,
                            email_notifications_enabled boolean NOT NULL,
                            radius_km double precision,
                            transport_mode varchar(255) CHECK (
                                transport_mode IN ('FOOT_WALKING','DRIVING_CAR','CYCLING_REGULAR')
                                ),
                            FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE volunteer_skills (
                                  volunteer_id uuid NOT NULL,
                                  skills varchar(255),
                                  FOREIGN KEY (volunteer_id) REFERENCES volunteers(user_id)
);

CREATE TABLE conversations (
                               id uuid PRIMARY KEY,
                               created_at timestamp NOT NULL,
                               updated_at timestamp,
                               related_entity_id uuid NOT NULL,
                               type varchar(255) NOT NULL CHECK (type IN ('DONATION','HELP_REQUEST'))
);

CREATE TABLE conversation_participants (
                                           id uuid PRIMARY KEY,
                                           joined_at timestamp NOT NULL,
                                           last_read_at timestamp,
                                           conversation_id uuid NOT NULL,
                                           user_id uuid NOT NULL,
                                           UNIQUE (conversation_id, user_id),
                                           FOREIGN KEY (conversation_id) REFERENCES conversations(id),
                                           FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE donations (
                           id uuid PRIMARY KEY,
                           created_at timestamp NOT NULL,
                           updated_at timestamp,
                           location geography(Point,4326),
                           active boolean NOT NULL,
                           cancel_reason varchar(255),
                           completed_at timestamp,
                           confirmed_at timestamp,
                           description text,
                           donation_type varchar(255) NOT NULL,
                           expiry_date timestamp,
                           food_type varchar(255),
                           picked_up_at timestamp,
                           quantity integer NOT NULL,
                           reserved_at timestamp,
                           status varchar(255) NOT NULL,
                           title varchar(255) NOT NULL,
                           unit varchar(30),
                           donor_id uuid NOT NULL,
                           volunteer_id uuid,
                           FOREIGN KEY (donor_id) REFERENCES users(id),
                           FOREIGN KEY (volunteer_id) REFERENCES volunteers(user_id)
);

CREATE TABLE help_requests (
                               id uuid PRIMARY KEY,
                               created_at timestamp NOT NULL,
                               updated_at timestamp,
                               location geography(Point,4326),
                               accepted_at timestamp,
                               active boolean NOT NULL,
                               cancel_reason varchar(255),
                               completed_at timestamp,
                               deadline timestamp,
                               description text,
                               status varchar(255) NOT NULL,
                               title varchar(255) NOT NULL,
                               type varchar(255) NOT NULL,
                               requester_id uuid NOT NULL,
                               volunteer_id uuid,
                               FOREIGN KEY (requester_id) REFERENCES users(id),
                               FOREIGN KEY (volunteer_id) REFERENCES volunteers(user_id)
);

CREATE TABLE messages (
                          id uuid PRIMARY KEY,
                          content text NOT NULL,
                          deleted boolean NOT NULL,
                          deleted_at timestamp,
                          sent_at timestamp NOT NULL,
                          conversation_id uuid NOT NULL,
                          sender_id uuid NOT NULL,
                          FOREIGN KEY (conversation_id) REFERENCES conversations(id),
                          FOREIGN KEY (sender_id) REFERENCES users(id)
);

CREATE TABLE proposals (
                           id uuid PRIMARY KEY,
                           created_at timestamp NOT NULL,
                           updated_at timestamp,
                           active boolean NOT NULL,
                           cancel_reason varchar(255),
                           responded_at timestamp,
                           score double precision,
                           status varchar(255) NOT NULL,
                           target_entity_id uuid NOT NULL,
                           type varchar(255) NOT NULL,
                           version bigint NOT NULL,
                           volunteer_id uuid NOT NULL,
                           FOREIGN KEY (volunteer_id) REFERENCES volunteers(user_id),
                           UNIQUE (volunteer_id, target_entity_id, type)
);

CREATE TABLE proposal_matching_state (
                                         entity_id uuid PRIMARY KEY,
                                         current_radius_meters integer NOT NULL,
                                         entity_type varchar(255),
                                         last_retry_at timestamp,
                                         retry_count integer NOT NULL,
                                         version bigint
);

CREATE TABLE reviews (
                         id uuid PRIMARY KEY,
                         created_at timestamp NOT NULL,
                         updated_at timestamp,
                         comment text,
                         rating integer NOT NULL CHECK (rating BETWEEN 1 AND 5),
                         author_id uuid NOT NULL,
                         donation_id uuid,
                         help_request_id uuid,
                         target_id uuid NOT NULL,
                         FOREIGN KEY (author_id) REFERENCES users(id),
                         FOREIGN KEY (target_id) REFERENCES users(id),
                         FOREIGN KEY (donation_id) REFERENCES donations(id),
                         FOREIGN KEY (help_request_id) REFERENCES help_requests(id),
                         UNIQUE (author_id, donation_id),
                         UNIQUE (author_id, help_request_id)
);

CREATE TABLE otp_codes (
                           id BIGSERIAL PRIMARY KEY,
                           code varchar(255) NOT NULL,
                           email varchar(255) NOT NULL,
                           expires_at timestamp NOT NULL,
                           type varchar(255) NOT NULL,
                           used boolean NOT NULL
);

CREATE TABLE pending_notifications (
                                       id uuid PRIMARY KEY,
                                       created_at timestamp NOT NULL,
                                       entity_id uuid NOT NULL,
                                       entity_title varchar(255) NOT NULL,
                                       entity_type varchar(255) NOT NULL,
                                       sent boolean NOT NULL,
                                       volunteer_email varchar(255) NOT NULL,
                                       volunteer_id uuid NOT NULL,
                                       volunteer_name varchar(255) NOT NULL
);