create table people
(
    first_name     varchar(20) not null,
    surname        varchar(20) not null,
    id_card_number char(18)    not null
        constraint people_pkey
            primary key,
    phone_number   varchar(15) not null,
    gender         char,
    student_or_not char
);

alter table people
    owner to tao;

create table stations
(
    id            integer     not null
        constraint stations_pkey
            primary key,
    station_name  varchar(30) not null,
    province_name varchar(30) not null
);

alter table stations
    owner to tao;

create table route_detail
(
    train_id       integer not null,
    station_number integer not null,
    station_id     integer
        constraint route_detail_stations_id_fk
            references stations,
    arrive_time    time    not null,
    depart_time    time    not null,
    id             serial  not null
        constraint route_detail_pk
            primary key,
    constraint route_detail_pkey
        unique (train_id, station_number)
);

alter table route_detail
    owner to tao;

create unique index stations_station_name_uindex
    on stations (station_name);

create table train_type_seat_type
(
    train_type     varchar not null,
    seat_type      integer not null,
    seat_type_name varchar not null,
    constraint table_name_pk
        primary key (train_type, seat_type)
);

alter table train_type_seat_type
    owner to tao;

create table ticket_detail
(
    id               serial           not null
        constraint ticket_detail_pk
            primary key,
    start_station_id integer          not null
        constraint ticket_price_stations_id_fk
            references stations,
    end_station_id   integer          not null
        constraint ticket_price_stations_id_fk_2
            references stations,
    train_type       varchar          not null,
    price            double precision not null,
    seat_type        integer          not null,
    constraint ticket_detail_train_type_seat_type_train_type_seat_type_fk
        foreign key (train_type, seat_type) references train_type_seat_type
);

alter table ticket_detail
    owner to tao;

create unique index ticket_detail_start_station_id_end_station_id_train_type_seat_t
    on ticket_detail (start_station_id, end_station_id, train_type, seat_type);

create table trains
(
    train_id         serial      not null
        constraint trains_pk
            primary key,
    train_code       varchar(20) not null,
    train_type       varchar(20) not null,
    start_station_id integer     not null
        constraint trains_stations_id_fk
            references stations,
    end_station_id   integer
        constraint trains_stations_id_fk_2
            references stations,
    status           integer,
    constraint train_code_train_type
        unique (train_code, train_type)
);

alter table trains
    owner to tao;

create table tickets
(
    ticket_id        serial   not null
        constraint tickets_pkey
            primary key,
    train_id         integer  not null
        constraint tickets_trains_train_id_fk
            references trains,
    id_card_number   char(18) not null
        constraint fk3
            references people,
    ticket_entrance  varchar(20),
    ticket_date      date     not null,
    ticket_type      char     not null,
    ticket_detail_id integer  not null
        constraint tickets_ticket_detail_id_fk
            references ticket_detail
);

alter table tickets
    owner to tao;

create unique index tickets_train_id_id_card_number_ticket_date_ticket_detail_id_ui
    on tickets (train_id, id_card_number, ticket_date, ticket_detail_id);

create table trains_stations_seats
(
    id              serial  not null
        constraint trains_stations_seats_pk
            primary key,
    seat_type       integer not null,
    route_detail_id integer not null
        constraint trains_stations_seats_route_detail_id_fk
            references route_detail
);

alter table trains_stations_seats
    owner to tao;

create table rest_tickets
(
    trains_stations_seats_id integer not null
        constraint rest_tickets_trains_stations_seats_id_fk
            references trains_stations_seats,
    date                     date    not null,
    ticket_num               integer default 5,
    constraint rest_tickets_pk
        primary key (trains_stations_seats_id, date)
);

alter table rest_tickets
    owner to tao;

create table users
(
    user_name      varchar(30) not null
        constraint pk
            primary key,
    password       varchar(30) not null,
    phone_number   varchar(15) not null,
    id_card_number char(18)    not null
        constraint fk
            references people,
    credit         integer,
    constraint unique_
        unique (user_name, id_card_number)
);

alter table users
    owner to tao;

create table orders
(
    order_id     serial      not null
        constraint orders_pkey
            primary key,
    user_name    varchar(30) not null
        constraint orders_user_fk
            references users,
    create_time  timestamp   not null,
    order_status integer     not null
);

alter table orders
    owner to tao;

create table orders_tickets
(
    id        serial  not null
        constraint orders_tickets_pk
            primary key,
    order_id  integer not null
        constraint orders_tickets_orders_order_id_fk
            references orders,
    ticket_id integer
        constraint orders_tickets_tickets_ticket_id_fk
            references tickets
);

alter table orders_tickets
    owner to tao;

create unique index orders_tickets_order_id_ticket_id_uindex
    on orders_tickets (order_id, ticket_id);

create unique index users_id_card_number_uindex
    on users (id_card_number);


