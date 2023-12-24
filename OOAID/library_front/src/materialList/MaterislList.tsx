// BookList.jsx
import React, {useState, useEffect} from 'react';
import {List, Button, Modal} from 'antd';
import AuthenticationForm from '../authorizitaion/AuthPage';
import PostService from "../postService/PostService";
import ReviewList from "../reviewList/ReviewList";
import {useParams} from "react-router-dom"; // Импортируйте компонент AuthenticationForm

const BookList = () => {
    const [data, setData]: any = useState();
    const [books, setBooks]: any = useState([]);
    const [reviews, setReviews]: any = useState([]);

    const {university, course, subject}: any = useParams();

    useEffect(() => {
        console.log("Запуск")
        // Загрузка списка книг для выбранного предмета
        PostService.getBooks(university, course, subject).then((response: any) => {
            const sortedCourses = response.data;
            console.log(sortedCourses)
            setData(sortedCourses);
            setBooks(sortedCourses.materials)
            setReviews(sortedCourses.reviews)
        });
    }, [university, course, subject]);

    const handleDownload = (book: any) => {

        const isUserAuthenticated = checkUserAuthentication(); // Реализуйте функцию checkUserAuthentication

        if (isUserAuthenticated) {
            // Выполнить скачивание книги
            downloadBook(book);
        } else {
            // Отобразить форму авторизации/регистрации
            showAuthenticationForm();
        }
    };

    const downloadBook = (book) => {
        // Здесь реализуйте логику скачивания книги
        console.log(`Загрузка книги: ${book.title}`);
    };

    const checkUserAuthentication = () => {

        // Временная заглушка (замените на реальную логику)
        const isAuthenticated = localStorage.getItem('token') !== null;
        return isAuthenticated;
    };

    const showAuthenticationForm = () => {
        // Отобразить модальное окно с формой авторизации/регистрации
        Modal.info({
            title: 'Внимание',
            content: (
                <div>
                    <p>Для скачивания книги необходимо войти или зарегистрироваться.</p>
                    {/* Интегрируйте компонент с формой авторизации/регистрации */}
                    <AuthenticationForm
                        onAuthenticationSuccess={handleAuthenticationSuccess}
                        onRegistrationSuccess={handleRegistrationSuccess}
                    />
                </div>
            ),
            onOk() {
                // Обработчик закрытия модального окна
                console.log('Модальное окно закрыто');
            },
        });
    };

    const handleAuthenticationSuccess = () => {
        // Обработчик успешной авторизации
        // Вы можете выполнить необходимые действия, например, обновить компонент или выполнить скачивание книги
        console.log('Успешная авторизация');
        // Продолжить скачивание книги или другие действия
        // ...

        // Закрыть модальное окно
        Modal.destroyAll();
    };

    const handleRegistrationSuccess = () => {
        // Обработчик успешной регистрации
        // Вы можете выполнить необходимые действия, например, обновить компонент или выполнить скачивание книги
        console.log('Успешная регистрация');
        // Продолжить скачивание книги или другие действия
        // ...

        // Закрыть модальное окно
        Modal.destroyAll();
    };

    return (
        <div>
            <header className="book-list-header">Список книг для {subject} предмета</header>
            <List
                dataSource={books}
                renderItem={(item: any) => (
                    <List.Item>
                        {item.title} - {item.author}
                        <Button onClick={() => handleDownload(item)}>Скачать</Button>
                    </List.Item>
                )}
            />
            <ReviewList university={university} course={course} selectedSubject={subject} inputReviews={reviews} />
        </div>
    );
};

export default BookList;
