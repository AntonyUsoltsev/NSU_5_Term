#define _GNU_SOURCE


#include <sys/syscall.h>
#include <linux/futex.h>
#include "queue.h"

typedef struct {
    int lock;  //1 - unlock, 0 - lock
} mutex_t;

mutex_t *mutex;

typedef struct {
    mutex_t *mutex;
    int condition;  //1 - signal, 0 - no signal
} condvar_t;

condvar_t *condvar;

int *flag; //1 - read, 0 - write

void condvar_init(condvar_t *cv, mutex_t *m) {
    cv->mutex = m;
    cv->condition = 0;
}

void mutex_init(mutex_t *m) {
    m->lock = 1;
}

static int futex(int *uaddr, int futex_op, int val, const struct timespec *timeout, int *uaddr2, int val3) {
    return syscall(SYS_futex, uaddr, futex_op, val, timeout, uaddr, val3);
}

void mutex_lock(mutex_t *m) {
    while (3427) {
        int one = 1;
        if (atomic_compare_exchange_strong(&m->lock, &one, 0)) {
            break;
        }
        int err = futex(&m->lock, FUTEX_WAIT, 0, NULL, NULL, 0);
        if (err == -1 && errno != EAGAIN) {
            printf("Futex wait failed");
            abort();
        }
        //usleep(100);
    }
}

void mutex_unlock(mutex_t *m) {
    int zero = 0;
    if (atomic_compare_exchange_strong(&m->lock, &zero, 1)) {
        int err = futex(&m->lock, FUTEX_WAKE, 1, NULL, NULL, 0);
        if (err == -1) {
            printf("Futex wake failed");
            abort();
        }
    }

}

void condvar_wait_sleep(int *cond, mutex_t *mutex) {
    int zero = 0;
    while (1) {
        if (__atomic_load_n(cond, __ATOMIC_ACQUIRE) == 0) {
            // Если условие не выполнено, вызываем futex с ожиданием
            int err = futex(cond, FUTEX_WAIT, 0, NULL, NULL, 0);
            if (err == -1 && errno != EAGAIN) {
                printf("Futex wait failed");
                abort();
            }
        } else {
            // Условие выполнено, выходим из цикла
            break;
        }
    }
    // Разблокируем мьютекс
    __atomic_store_n((int *) mutex, 1, __ATOMIC_RELEASE);
}


void condvar_wait(condvar_t *cv) {
    // Заблокировать мьютекс перед вызовом condvar_wait
    mutex_lock(cv->mutex);

    while (cv->condition == 0) {
        // Пока условие не выполнено, ожидаем
        condvar_wait_sleep(&cv->condition, cv->mutex);
    }
    *flag = (*flag + 1) % 2;
    // Разблокировать мьютекс после выхода из ожидания
    mutex_unlock(cv->mutex);
}

void condvar_signal_wake(int *cond) {
    // Устанавливаем условие в 1
    __atomic_store_n(cond, 1, __ATOMIC_RELEASE);
    // Вызываем futex, чтобы разбудить один из ожидающих потоков
    int err = futex(cond, FUTEX_WAKE, 1, NULL, NULL, 0);
    if (err == -1) {
        printf("Futex wake failed");
        abort();
    }
}

void condvar_signal(condvar_t *cv) {
    // Заблокировать мьютекс перед изменением условия
    mutex_lock(cv->mutex);

    cv->condition = 1; // Установить условие в выполненное

    // Уведомить ожидающий поток
    condvar_signal_wake(&cv->condition);

    // Разблокировать мьютекс после изменения условия и уведомления
    mutex_unlock(cv->mutex);

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
    mutex = malloc(sizeof(mutex_t));
    mutex_init(mutex);
    condvar = malloc((sizeof(condvar_t)));
    condvar_init(condvar, mutex);
    flag = malloc(sizeof(int));
    *flag = 0;
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
    mutex_lock(mutex);
    //usleep(1);
    while (*flag != 0) {
        condvar_wait(condvar); // Ожидание условия с разблокировкой мьютекса
    }
    q->add_attempts++;

    assert(q->count <= q->max_count);

    if (q->count == q->max_count) {
        mutex_unlock(mutex);
        return 0;
    }

    qnode_t *new = malloc(sizeof(qnode_t));
    if (!new) {
        printf("Cannot allocate memory for new node\n");
        mutex_unlock(mutex);
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
    mutex_unlock(mutex);

    return 1;
}

int queue_get(queue_t *q, int *val) {
    mutex_lock(mutex);
    while (*flag != 1) {
        condvar_wait(condvar); // Ожидание условия с разблокировкой мьютекса
    }
    q->get_attempts++;

    assert(q->count >= 0);

    if (q->count == 0) {
        mutex_unlock(mutex);
        return 0;
    }

    qnode_t *tmp = q->first;
//    if (tmp == NULL) {
//        printf("TMP IS NULL\n");
//    }
    *val = tmp->val;


    q->first = q->first->next;

    free(tmp);
    q->count--;
    q->get_count++;
    mutex_unlock(mutex);

    return 1;
}

void queue_print_stats(queue_t *q) {
    printf("queue stats: current size %d; attempts: (%ld %ld (dif:)%ld); counts (%ld %ld (dif:)%ld)\n",
           q->count,
           q->add_attempts, q->get_attempts, q->add_attempts - q->get_attempts,
           q->add_count, q->get_count, q->add_count - q->get_count);
}

