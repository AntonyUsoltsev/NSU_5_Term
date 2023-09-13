

#define _GNU_SOURCE

#include <stdio.h>
#include <pthread.h>
#include <string.h>
#include <errno.h>
#include <sys/types.h>
#include <unistd.h>


void *mythread(void *arg) {
    int local_var = 1;
    static int st_local_var = 2;
    const int const_loc_var = 3;
    printf("mythread [%d %d %d]: Hello from mythread!\n", getpid(), getppid(), gettid());
    return NULL;
}


int main() {
    pthread_t tid;
    pthread_t tid_arr[5];
    int err;

    printf("main [%d %d %d]: Hello from main!\n", getpid(), getppid(), gettid());
    sleep(15);
    for (int i = 0; i < 5; i++) {
        //err = pthread_create(&tid, NULL, mythread, NULL);

        err = pthread_create(&tid_arr[i], NULL, mythread, &tid_arr[i]);
        printf("in main create new thread with tid %ld\n", tid_arr[i]);
        if (err) {
            printf("main: pthread_create() failed: %s\n", strerror(err));
            return -1;
        }
        sleep(5);
    }
    printf("new changes");
    sleep(30); // для того чтобы успел выполнится второй поток

    return 0;
}

