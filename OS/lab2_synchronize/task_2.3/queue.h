#ifndef __LAB2_SYNCHRONIZE__
#define __LAB2_SYNCHRONIZE__

#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <string.h>

#define MAX_STRING_LENGTH 100

typedef struct _Node {
    char value[MAX_STRING_LENGTH];

    struct _Node *next;
    pthread_mutex_t sync;
} Node;

typedef struct _Storage {
    Node *first;
} Storage;

typedef struct _ThreadData {
    Storage *storage;
    int *counter;
} ThreadData;


Storage* initialize_storage(int capacity);

void add_node(Storage *storage, const char *value);

void fill_storage(Storage *storage);

void print_storage(Storage *storage);

#endif// __LAB2_SYNCHRONIZE__
