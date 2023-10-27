#include <stdio.h>
#include <pthread.h>
#include <string.h>
#include <stdlib.h>

volatile int val = 0;

void *adder1() {
    for (int i = 0; i < 10000000; ++i) {
        val++;
    }
    return NULL;
}

void *adder2() {
    for (int i = 0; i < 10000000; ++i) {
        val++;
    }
    return NULL;
}

int main() {
    pthread_t tid1, tid2;
    int err = pthread_create(&tid1, NULL, adder1, NULL);
    if (err) {
        exit(1);
    }

    err = pthread_create(&tid2, NULL, adder2, NULL);
    if (err) {
        exit(1);
    }

    void *retval1, *retval2;

    pthread_join(tid1, &retval1);
    pthread_join(tid2, &retval2);

    printf("val: %d\n", val);
}