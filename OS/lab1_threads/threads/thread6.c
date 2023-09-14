typedef struct {

}mythread_t;

int mythread_create(mythread_t *mytid,start_routine, void* arg){

}

void * mythread(void* args){
    for (int i = 0; i < 5; ++i) {
        printf(args);
        sleep(1);
    }

}

int main(){
    mythread_t tid;
    mythread_create(&tid, &mythread, "Hello");
    mythread_join(tid);

}

