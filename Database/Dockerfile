FROM postgres:11.3-alpine
COPY ./init.sql /docker-entrypoint-initdb.d/
ENV POSTGRES_PASSWORD=javeriana
ENV POSTGRES_USER=vcloud