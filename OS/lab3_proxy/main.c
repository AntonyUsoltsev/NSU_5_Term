#include <stdio.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <unistd.h>
#include <string.h>
#include <pthread.h>
#include <netdb.h>
#include <signal.h>
#include <arpa/inet.h>
#include "cache_list.h"
#include "logger.h"

#define FAIL (-1)
#define PORT 80
#define MAX_USERS_COUNT 10
#define BUFFER_SIZE 4096

typedef struct {
    int client_socket;
    char *request;
} context;

int server_is_on = 1;
Cache *cache;

void sigint_handler(int signo) {
    if (signo == SIGINT) {
        logg("Shutting down the server", BLUE);
        server_is_on = 0;
    }
}

int init_cache() {
    logg("Initializing cache", YELLOW);
    cache = malloc(sizeof(Cache));
    if (cache == NULL) {
        return EXIT_FAILURE;
    }
    cache->next = NULL;
    return EXIT_SUCCESS;
}

void destroy_cache() {
    logg("Destroying cache", YELLOW);
    pthread_mutex_lock(&cache_mutex);
    Cache *cur = cache;
    while (cur != NULL) {
        delete_cache_record(cur);
        Cache *next = cur->next;
        free(cur);
        cur = next;
    }
    pthread_mutex_unlock(&cache_mutex);
}

int create_server_socket() {
    struct sockaddr_in server_addr;
    int server_socket = socket(AF_INET, SOCK_STREAM, 0);
    if (server_socket == -1) {
        logg("Error while creating server socket", RED);
        return FAIL;
    }

    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_UNSPEC;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = htons(PORT);
    logg("Server socket created", GREEN);

    int err = bind(server_socket, (struct sockaddr *) &server_addr, sizeof(server_addr));
    if (err == FAIL) {
        logg("Failed to bind server socket", RED);
        close(server_socket);
        return FAIL;
    }

    logg_int("Server socket bound to ", server_addr.sin_addr.s_addr, GREEN);

    err = listen(server_socket, MAX_USERS_COUNT);
    if (err == FAIL) {
        logg("Server socket failed to listen", RED);
        close(server_socket);
        return FAIL;
    }
    return server_socket;
}

int read_request(int client_socket, char *request) {
    // Get HTTP-request from client
    ssize_t bytes_read = read(client_socket, request, BUFFER_SIZE);
    if (bytes_read < 0) {
        logg("Error while read request", RED);
        close(client_socket);
        return EXIT_FAILURE;
    }
    if (bytes_read == 0) {
        logg("Connection closed from client", RED);
        close(client_socket);
        return EXIT_FAILURE;
    }
    request[bytes_read] = '\0';
    logg_char("Received request:\n", request, GREEN);
    return EXIT_SUCCESS;
}

int send_from_cache(char *request, int client_socket) {
    char *cache_record = calloc(CACHE_BUFFER_SIZE, sizeof(char));
    ssize_t len = find_in_cache(cache, request, cache_record);

    if (len != -1) {
        // If response find in cache send it to client
        ssize_t send_bytes = write(client_socket, cache_record, len);
        if (send_bytes == FAIL) {
            logg("Error while sending cached data", RED);
            close(client_socket);
            free(cache_record);
            return EXIT_FAILURE;
        }
        free(cache_record);
        logg_int("Send cached response to the client, len = ", send_bytes, PURPLE);
        printf("\n");
        close(client_socket);
        return EXIT_SUCCESS;
    }
    return EXIT_FAILURE;
}

int connect_to_remote(char *host) {
    struct addrinfo hints, *res0;
    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;

    // Getting remote server info
    int status = getaddrinfo((char *) host, "http", &hints, &res0);
    if (status != 0) {
        logg("getaddrinfo error", RED);
        freeaddrinfo(res0);
        return -1;
    }
    int dest_socket = socket(res0->ai_family, res0->ai_socktype, res0->ai_protocol);
    if (dest_socket == FAIL) {
        logg("Error while creating remote server socket", RED);
        return FAIL;
    }

    int err = connect(dest_socket, res0->ai_addr, res0->ai_addrlen);
    if (err == FAIL) {
        logg("Error while connecting to remote server", RED);
        close(dest_socket);
        freeaddrinfo(res0);
        return FAIL;
    }
    return dest_socket;
}


void *client_handler(void *arg) {
    context *ctx = (context *) arg;
    int client_socket = ctx->client_socket;
    char *request0 = ctx->request;
    char request[BUFFER_SIZE];
    strcpy(request, request0);

    Cache *record = malloc(sizeof(Cache));
    init_cache_record(record);
    add_request(record, request0);

    // Creating new connection with remote server
    // Parse the request and extract the host
    unsigned char host[50];
    const unsigned char *host_result = memccpy(host, strstr((char *) request, "Host:") + 6, '\r', sizeof(host));
    host[host_result - host - 1] = '\0';
    logg_char("Remote server host name: ", (char *) host, GREEN);

    int dest_socket = connect_to_remote((char *) host);
    if (dest_socket == FAIL) {
        close(client_socket);
    }
    logg("Create new connection with remote server", GREEN);

    ssize_t bytes_sent = write(dest_socket, request, sizeof(request));
    if (bytes_sent == FAIL) {
        logg("Error while sending request to remote server", RED);
        close(client_socket);
        close(dest_socket);
        return NULL;
    }
    logg_int("  Send request to remote server, len = ", bytes_sent, GREEN);

    char *buffer = calloc(BUFFER_SIZE, sizeof(char));
    ssize_t bytes_read, all_bytes_read = 0;
    while ((bytes_read = read(dest_socket, buffer, BUFFER_SIZE)) > 0) {
//        logg_int("    Read response from remote server, len = ", bytes_read, GREEN);
        bytes_sent = write(client_socket, buffer, bytes_read);
        if (bytes_sent == -1) {
            logg("Error while sending data to client", RED);
            close(client_socket);
            close(dest_socket);
            return NULL;
        } else {
            // Cache part of response
//            logg_int("\tWrite response to client, len = ", bytes_sent, GREEN);
            add_response(record, buffer, all_bytes_read, bytes_read);
//            logg_int("\tCached part of response, len = ", bytes_sent, GREEN);
        }
        all_bytes_read += bytes_read;
    }
    add_size(record, all_bytes_read);
    push_record(cache, record);
    logg_int("Cached the result, len = ", all_bytes_read, BLUE);
    printf("\n");

    close(client_socket);
    close(dest_socket);
    free(buffer);
    free(request0);

    return NULL;
}

int main() {
    logg("SERVER START", BACK_PURP);
    signal(SIGINT, sigint_handler);

    int server_socket = create_server_socket();
    if (server_socket == FAIL) {
        logg("Error to create server socket", RED);
        exit(EXIT_FAILURE);
    }

    int err = init_cache();
    if (err == EXIT_FAILURE) {
        logg("Error to init cache", RED);
        destroy_cache();
        close(server_socket);
        exit(EXIT_FAILURE);
    }

    logg_int("Server listening on port ", PORT, PURPLE);

    while (server_is_on) {
        int client_socket;
        struct sockaddr_in client_addr;
        socklen_t client_addr_size = sizeof(client_addr);
        client_socket = accept(server_socket, (struct sockaddr *) &client_addr, &client_addr_size);
        if (client_socket == FAIL) {
            logg("Failed to accept", RED);
            close(server_socket);
            destroy_cache();
            exit(EXIT_FAILURE);
        }
        char *buff = calloc(BUFFER_SIZE, sizeof(char));
        sprintf(buff, "Client connected from %s:%d", inet_ntoa(client_addr.sin_addr), ntohs(client_addr.sin_port));
        logg(buff, BLUE);
        free(buff);

        char *request = calloc(BUFFER_SIZE, sizeof(char));
        err = read_request(client_socket, request);
        if (err == EXIT_FAILURE) {
            logg("Failed to read request", RED);
            free(request);
            close(client_socket);
            continue;
        }

        if (send_from_cache(request, client_socket) == EXIT_SUCCESS) {
            free(request);
            close(client_socket);
            continue;
        } else {
            logg("Init new connection", PURPLE);
            context ctx = {client_socket, request};
            pthread_t handler_thread;
            err = pthread_create(&handler_thread, NULL, &client_handler, &ctx);
            if (err == -1) {
                logg("Failed to create thread", RED);
                close(client_socket);
                close(server_socket);
                destroy_cache();
                exit(EXIT_FAILURE);
            }
        }
    }
    close(server_socket);
    destroy_cache();
    exit(EXIT_SUCCESS);
}