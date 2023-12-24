// BookList.jsx
import React, {useState, useEffect} from 'react';
import {List, Button, message, Spin} from 'antd';
import PostService from "../postService/PostService";
import ReviewList from "../reviewList/ReviewList";
import {useHistory, useParams} from "react-router-dom";
import "./MaterialStyle.css"
import axios from "axios";

const BookList = () => {
    const [data, setData]: any = useState();
    const [books, setBooks]: any = useState([]);
    const [reviews, setReviews]: any = useState([]);
    const [loading, setLoading] = useState(true);

    const {university, course, subject}: any = useParams();
    const history = useHistory();

    useEffect(() => {
        // Загрузка списка книг для выбранного предмета
        PostService.getBooks(university, course, subject).then((response: any) => {
            console.log("start")
            console.log(response)
            const inputData = response.data;
            setData(inputData);
            setBooks(inputData.materials)
            setReviews(inputData.reviews)
            setLoading(false);
        });
    }, [university, course, subject]);

    const handleDownload = (book: any) => {
        const isUserAuthenticated = checkUserAuthentication();

        if (isUserAuthenticated) {
            // Выполнить скачивание книги
            downloadBook(book);
        } else {
            // Показать предупреждение
            showWarning();
        }
    };

    const downloadBook = (book) => {
        // Здесь реализуйте логику скачивания книги
        console.log(`Загрузка книги: ${book.title}`);
    };

    const checkUserAuthentication = () => {
        const token = localStorage.getItem('token');
        if (!token) {
            return false;
        }

        // Выполнение запроса к бэкенду для проверки авторизации
        return axios.get('ваш-эндпоинт-проверки-авторизации', {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        })
            .then(response => {
                // В данном случае, успешный ответ считается подтверждением авторизации
                return true;
            })
            .catch(error => {
                // В случае ошибки, например, если токен недействителен
                return false;
            });
    };

    const showWarning = () => {
        message.warning('Прежде чем скачать, необходимо авторизоваться.');
    };
    const handleBackClick = () => {
        const booksRoute = "/";
        history.push(booksRoute);
        window.location.reload();
    }

    return (
        <div>
            {loading ? (
                <Spin size="large"/>
            ) : (
                <>
                    <header className="subjects-header">Список книг для предмета {data.name}</header>
                    <List
                        dataSource={books}
                        renderItem={(item: any) => (
                            <List.Item>
                                {item.title} - {item.author}
                                <Button onClick={() => handleDownload(item)}>Скачать</Button>
                            </List.Item>
                        )}
                    />
                    <ReviewList selectedSubject={subject} selectedSubjectName={data.name} inputReviews={reviews}/>
                    <Button type="primary" onClick={handleBackClick}>
                        Назад
                    </Button>
                </>
            )}
        </div>
    );
};

export default BookList;
