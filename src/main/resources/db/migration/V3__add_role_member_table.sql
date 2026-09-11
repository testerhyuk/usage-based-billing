alter table member
    add column role enum('ROLE_ADMIN', 'ROLE_MEMBER') NOT NULL default 'ROLE_MEMBER';