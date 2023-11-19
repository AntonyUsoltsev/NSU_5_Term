#define _GNU_SOURCE

#include <pthread.h>
#include <assert.h>

#include "queue.h"

typedef struct {
    int lock;  //1 - unlock, 0 - lock
} spinlock_t;

spinlock_t *spin;

void spinlock_init(spinlock_t *s) {
    s->lock = 1;
}

void spinlock_lock(spinlock_t *s) {
    while (1) {
        int one = 1;
        if (atomic_compare_exchange_strong(&s->lock, &one, 0)) {
            break;
        }
        //usleep(100);
    }
}

void spinlock_unlock(spinlock_t *s) {
    int zero = 0;
    atomic_compare_exchange_strong(&s->lock, &zero, 1);
}

void *qmonitor(void *arg) {
    queue_t *q = (queue_t *) arg;

    printf("qmonitor: [%d %d %d]\n", getpid(), getppid(), gettid());

    while (1) {
        queue_print_stats(q);
        // usleep(5000);
        sleep(1);
    }

    return NULL;
}

queue_t *queue_init(int max_count) {

    int err;
    spin = malloc(sizeof(spinlock_t));
    spinlock_init(spin);
    queue_t *q = malloc(sizeof(queue_t));
    if (!q) {
        printf("Cannot allocate memory for a queue\n");
        abort();
    }

    q->first = NULL;
    q->last = NULL;
    q->max_count = max_count;
    q->count = 0;

    q->add_attempts = q->get_attempts = 0;
    q->add_count = q->get_count = 0;

    err = pthread_create(&q->qmonitor_tid, NULL, qmonitor, q);
    if (err) {
        printf("queue_init: pthread_create() failed: %s\n", strerror(err));
        abort();
    }

    return q;
}

void queue_destroy(queue_t *q) {
    for (int i = 0; i < q->count; ++i) {
        qnode_t *tmp = q->first;
        q->first = q->first->next;
        free(tmp);
    }
    q->count = 0;
    q->first = NULL;
    q->last = NULL;
}

int queue_add(queue_t *q, int val) {
    spinlock_lock(spin);
    //usleep(1);
    q->add_attempts++;

    assert(q->count <= q->max_count);

    if (q->count == q->max_count) {
        spinlock_unlock(spin);
        return 0;
    }

    qnode_t *new = malloc(sizeof(qnode_t));
    if (!new) {
        printf("Cannot allocate memory for new node\n");
        spinlock_unlock(spin);
        abort();
    }

    new->val = val;
    new->next = NULL;


    if (!q->first)
        q->first = q->last = new;
    else {
        q->last->next = new;
        q->last = q->last->next;
    }
    q->count++;
    q->add_count++;
    spinlock_unlock(spin);

    return 1;
}

int queue_get(queue_t *q, int *val) {
    spinlock_lock(spin);
    q->get_attempts++;

    assert(q->count >= 0);

    if (q->count == 0) {
        spinlock_unlock(spin);
        return 0;
    }

    qnode_t *tmp = q->first;
    *val = tmp->val;
    q->first = q->first->next;

    free(tmp);
    q->count--;
    q->get_count++;
    spinlock_unlock(spin);

    return 1;
}

void queue_print_stats(queue_t *q) {
    printf("queue stats: current size %d; attempts: (%ld %ld (dif:)%ld); counts (%ld %ld (dif:)%ld)\n",
           q->count,
           q->add_attempts, q->get_attempts, q->add_attempts - q->get_attempts,
           q->add_count, q->get_count, q->add_count - q->get_count);
}

