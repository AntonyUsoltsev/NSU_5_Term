#define _GNU_SOURCE

#include <stdio.h>
#include <pthread.h>
#include <string.h>
#include <errno.h>
#include <sys/types.h>
#include <unistd.h>

int global_var = 10;

void *mythread(void *arg) {
    int local_var = 1;
    static int st_local_var = 2;
    const int const_loc_var = 3;
    printf("mythread [%d %d %d]: Hello from mythread!\n", getpid(), getppid(), gettid());
    pthread_t tid = pthread_self();
    printf("tid insise thread = %ld\n", tid);
    printf("Result of comparission: == : %d, equals: %d\n", tid == *(pthread_t *) arg,pthread_equal(tid, *(pthread_t *) arg));
    printf("Var adresses: local %p, static_local %p, const_local %p, global %p\n", &local_var, &st_local_var, &const_loc_var, &global_var);
    local_var ++;
    global_var *= 2;
    printf("local + 1 = %d, global * 2 = %d\n", local_var, global_var);

    return NULL;
}

// c - i
// Идентификаторы потоков POSIX не совпадают с идентификаторами потоков, возвращаемыми gettid() системным вызовом,
// специфичным для Linux. Идентификаторы потоков POSIX назначаются и поддерживаются реализацией потоков.
// Возвращаемый идентификатор потока gettid()представляет собой число (аналогично идентификатору процесса),
// присвоенное ядром.

// Тип pthread_t трактуется стандартом POSIX-2001 как абстрактный. На уровне языка C он может быть представлен,
// например, структурой. Для работы со значениями типа pthread_t предусмотрены два метода: присваивание и сравнение
// на равенство, реализуемое функцией pthread_equal()

// c - ii
//Адреса переменных:
//  у локальной и константной локальной разные
//  у статической локальной и глобальной одинаковые

// d
// Локальная перемнная в каждом потоке своя поэтому изменений не видно
// Глобальная явлется разделяемой перменной

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
    sleep(30); // для того чтобы успел выполнится второй поток

    return 0;
}

