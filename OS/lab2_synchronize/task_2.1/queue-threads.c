#define _GNU_SOURCE

#include <stdio.h>
#include <pthread.h>
#include <string.h>
#include <unistd.h>

#include <sched.h>
#include <signal.h>

#include "queue.h"
#include "queue.c"

#define RED "\033[41m"
#define NOCOLOR "\033[0m"


queue_t *queue;

void sigsegv_handler(int signo) {
    printf("Received SIGSEGV, first: %p\n", queue->first);
    abort();
}


void set_cpu(int n) {
    int err;
    cpu_set_t cpuset;
    pthread_t tid = pthread_self();

    CPU_ZERO(&cpuset);  //Очищается набор процессоров cpuset
    CPU_SET(n, &cpuset); //Добавляется указанный процессор n в cpuset,

    err = pthread_setaffinity_np(tid, sizeof(cpu_set_t), &cpuset);
    if (err) {
        printf("set_cpu: pthread_setaffinity failed for cpu %d\n", n);
        return;
    }

    printf("set_cpu: set cpu %d\n", n);
}

void *reader(void *arg) {
    int expected = 0;
    queue_t *q = (queue_t *) arg;
    printf("reader [%d %d %d]\n", getpid(), getppid(), gettid());

    set_cpu(1);

    while (1) {
        int val = -1;
        //usleep(10);
        int ok = queue_get(q, &val);
        if (!ok)
            continue;

        if (expected != val)
            printf(RED"ERROR: get value is %d but expected - %d" NOCOLOR "\n", val, expected);

        expected = val + 1;
    }

    return NULL;
}

void *writer(void *arg) {
    int i = 0;
    queue_t *q = (queue_t *) arg;
    printf("writer [%d %d %d]\n", getpid(), getppid(), gettid());

    set_cpu(1);

    while (1) {
        int ok = queue_add(q, i);
        if (!ok)
            continue;
        i++;
    }

    return NULL;
}

int main() {
    pthread_t tid[2];

    int err;
    signal(11, sigsegv_handler);

    printf("main [%d %d %d]\n", getpid(), getppid(), gettid());

    queue = queue_init(10000000);


    err = pthread_create(&tid[0], NULL, reader, queue);
    if (err) {
        printf("main: pthread_create() failed: %s\n", strerror(err));
        return -1;
    }

    //  sched_yield();

    err = pthread_create(&tid[1], NULL, writer, queue);
    if (err) {
        printf("main: pthread_create() failed: %s\n", strerror(err));
        return -1;
    }

    void *retval1, *retval2;
    pthread_join(tid[0], &retval1);
    pthread_join(tid[1], &retval2);

    pthread_exit(NULL);

    return 0;
}
