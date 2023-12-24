// ReviewList.jsx
import React, {useEffect, useState} from 'react';
import {List, Avatar, Button, message} from 'antd';
import "./ReviewStyle.css"
import TextArea from "antd/es/input/TextArea";
import axios from "axios";

const ReviewList = ({universityName, courseValue, selectedSubject, inputReviews}) => {
    const [reviews, setReviews] = useState([]);
    const [newReviewText, setNewReviewText] = useState('');

    useEffect(() => {
        console.log(inputReviews)
        setReviews(inputReviews)
    }, [selectedSubject]);
    const handleReviewSubmit = () => {
        // Получение токена из localStorage
        const token = localStorage.getItem('token');

        // Проверка наличия токена
        if (!token) {
            message.warning('Чтобы оставить отзыв, необходимо авторизоваться.');
            return;
        }

        // Отправка запроса на бэкэнд с текстом отзыва и токеном пользователя
        axios.post(`http://localhost:8080/student_compass/review/${universityName}/${courseValue}/${selectedSubject}`, {
            text: newReviewText,
        }, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        })
            .then(response => {
                message.success('Отзыв успешно добавлен.');
                // Обновление списка отзывов после успешной отправки
                setReviews([...reviews, response.data]);
                // Очистка поля ввода
                setNewReviewText('');
            })
            .catch(error => {
                console.error('Ошибка при добавлении отзыва:', error);
                message.error('Ошибка при добавлении отзыва. Пожалуйста, попробуйте еще раз.');
            });
    };


    return (
        <div>
            <header className="reviews-header">Отзывы по предмету {selectedSubject}</header>
            <div>
                <TextArea
                    rows={4}
                    value={newReviewText}
                    onChange={(e) => setNewReviewText(e.target.value)}
                    placeholder="Введите ваш отзыв"
                />
                <Button type="primary" onClick={handleReviewSubmit}>
                    Оставить отзыв
                </Button>
            </div>
            <List
                itemLayout="horizontal"
                dataSource={reviews}
                renderItem={(review) => (
                    <List.Item>
                        <List.Item.Meta
                            avatar={<Avatar>{review.user.firstName.charAt(0)}</Avatar>}
                            title={review.user.firstName + " " + review.user.lastName}
                            description={review.value}
                        />
                    </List.Item>
                )}
            />
        </div>
    );
};


export default ReviewList;
