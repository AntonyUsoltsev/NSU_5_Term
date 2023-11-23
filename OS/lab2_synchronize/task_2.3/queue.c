#include "queue.h"

int storage_capacity;

Storage *initialize_storage(int capacity) {
    Storage *storage = malloc(sizeof(Storage));
    if (!storage) {
        printf("Failed to allocate memory for a queue\n");
        abort();
    }
    storage_capacity = capacity;
    storage->first = NULL;
    return storage;
}

void add_node(Storage *storage, const char *value) {
    Node *new_node = (Node *) malloc(sizeof(Node));

    if (!new_node) {
        perror("Failed to allocate memory for a new node");
        exit(EXIT_FAILURE);
    }
    if (storage->first != NULL) {
        Node *node = storage->first;
        while (node->next != NULL) {
            node = node->next;
        }
        node->next = new_node;
    } else {
        storage->first = new_node;
    }
    strcpy(new_node->value, value);
    new_node->next = NULL;
    pthread_mutex_init(&(new_node->sync), NULL);
}

void fill_storage(Storage *storage) {
    for (int i = 0; i < storage_capacity; ++i) {
        char buff[10];
        sprintf(buff, "%d", (i) % storage_capacity);
        add_node(storage, buff);
    }
}

void print_storage(Storage *storage) {
    Node *current = storage->first;
    while (current != NULL) {
        printf("%s ", current->value);
        current = current->next;
    }
    printf("\n");
}
