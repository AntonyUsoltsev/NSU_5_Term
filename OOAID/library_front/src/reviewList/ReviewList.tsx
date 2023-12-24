// ReviewList.jsx
import React, {useEffect, useState} from 'react';
import {List, Avatar} from 'antd';
import "./ReviewStyle.css"
const ReviewList = ({selectedSubject, inputReviews}) => {
    const [reviews, setReviews] = useState([]);
    useEffect(() => {
        console.log(inputReviews)
        setReviews(inputReviews)
    }, [ selectedSubject]);

    return (
        <div>
            <header className="reviews-header">Отзывы по предмету {selectedSubject}</header>
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
