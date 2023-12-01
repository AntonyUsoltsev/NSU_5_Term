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

#define RED   "\033[1;31m"
#define GREEN "\033[1;32m"
#define BLUE  "\033[1;34m"
#define RESET "\033[0m"
#define BACK_WHITE "\033[1;47m"
#define BACK_PURP "\033[1;45m"

#define FAIL (-1)
#define PORT 80
#define MAX_USERS_COUNT 10
#define MAX_CACHE_SIZE 128
#define BUFFER_SIZE 4096

typedef struct {
    char *request;
    char *response;
} cache_entry;

typedef struct {
    int client_socket;
    char *request;
} context;

cache_entry cache[MAX_CACHE_SIZE];
pthread_mutex_t cache_mutex = PTHREAD_MUTEX_INITIALIZER;
int cur_cache_pos = 0;
int server_is_on = 1;


void logg(char *msg, char *color) {
    pthread_t thread_id = pthread_self();
    if (strcmp(color, RED) == 0) {
        char buf[BUFFER_SIZE];
        sprintf(buf, "%s[Thread %ld] %s%s", color, thread_id, msg, RESET);
        perror(buf);
    } else {
        printf("%s[Thread %ld] %s%s\n", color, thread_id, msg, RESET);
    }
    fflush(stdout);
}

void logg_char(char *msg, char *info, char *color) {
    char buf[BUFFER_SIZE + 100];
    sprintf(buf, "%s %s", msg, info);
    logg(buf, color);
}

void logg_int(char *msg, long info, char *color) {
    char buf[BUFFER_SIZE + 100];
    sprintf(buf, "%s %ld", msg, info);
    logg(buf, color);
}

void sigint_handler(int signo) {
    if (signo == SIGINT) {
        logg("Shutting down the server", BLUE);
        server_is_on = 0;
    }
}

int init_cache() {
    for (int i = 0; i < MAX_CACHE_SIZE; ++i) {
        cache[i].request = calloc(BUFFER_SIZE, sizeof(char));
        cache[i].response = calloc(BUFFER_SIZE * 10, sizeof(char));
        if (cache[i].request == NULL || cache[i].response == NULL) {
            logg("Error in сalloc while init cache", RED);
            return FAIL;
        }
    }
    return EXIT_SUCCESS;
}

void destroy_cache() {
    for (int i = 0; i < MAX_CACHE_SIZE; ++i) {
        free(cache[i].request);
        free(cache[i].response);
    }
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

    logg_int("Server socket bound to", server_addr.sin_addr.s_addr, GREEN);

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

int find_in_cache(char *request, int client_socket) {
    int cache_index = -1;
    char *cache_record = calloc(BUFFER_SIZE * 10, sizeof(char));
    pthread_mutex_lock(&cache_mutex);
    for (int i = 0; i < MAX_CACHE_SIZE; ++i) {
        if (strcmp(cache[i].request, request) == 0) {
            cache_index = i;
            strcpy(cache_record, cache[i].response);
            break;
        }
    }
    pthread_mutex_unlock(&cache_mutex);

    if (cache_index != -1) {
        // If response find in cache send it to client
        ssize_t send_bytes = send(client_socket, cache_record, strlen(cache_record), 0);
        if (send_bytes == FAIL) {
            logg("Error while sending cached data", RED);
            close(client_socket);
            free(cache_record);
            return EXIT_FAILURE;
        }
        free(cache_record);
        logg_int("Send cached response to the client, len = ", send_bytes, BLUE);
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

    // Cache the request and shift current position in cache array
    pthread_mutex_lock(&cache_mutex);
    int cache_pos = cur_cache_pos;
    cur_cache_pos = (cur_cache_pos + 1) % MAX_CACHE_SIZE;
    strncpy(cache[cache_pos].request, request, sizeof(request));
    cache[cache_pos].response[0] = '\0';
    pthread_mutex_unlock(&cache_mutex);

    char *buffer = calloc(BUFFER_SIZE * 10, sizeof(char));
    ssize_t bytes_read;
    while ((bytes_read = read(dest_socket, buffer, BUFFER_SIZE)) > 0) {
        logg_int("\tRead response from remote server, len = ", bytes_read, GREEN);
        bytes_sent = write(client_socket, buffer, bytes_read);
        if (bytes_sent == -1) {
            logg("Error while sending data to client", RED);
            close(client_socket);
            close(dest_socket);
            return NULL;
        } else {
            logg_int("\t  Write response to client, len = ", bytes_sent, GREEN);
            buffer[bytes_read] = '\0';

            // Cache part of response
            pthread_mutex_lock(&cache_mutex);
            strcat(cache[cache_pos].response, buffer);
            pthread_mutex_unlock(&cache_mutex);
        }
    }
    close(client_socket);
    close(dest_socket);
    free(buffer);
    logg("Cached the result\n", BLUE);

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
    logg_int("Server listening on port", PORT, BACK_WHITE);

    int err = init_cache();
    if (err == -1) {
        logg("Error to init cache", RED);
        destroy_cache();
        close(server_socket);
        exit(EXIT_FAILURE);
    }

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
            free(request);
            close(client_socket);
            continue;
        }

        if (find_in_cache(request, client_socket) == EXIT_SUCCESS) {
            free(request);
            close(client_socket);
            continue;
        } else {
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
            pthread_join(handler_thread, NULL);
            free(request);
            close(client_socket);

        }
    }
    close(server_socket);
    destroy_cache();
    exit(EXIT_SUCCESS);
}
