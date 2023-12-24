// ReviewList.jsx
import React, {useEffect, useState} from 'react';
import {List, Avatar} from 'antd';
import PostService from "../postService/PostService";

const ReviewList = ({university, course, selectedSubject, inputReviews}) => {
    const [reviews, setReviews] = useState([]);
    useEffect(() => {
        setReviews(inputReviews)
    }, [university, course, selectedSubject]);

    return (
        <div>
            <h2>Отзывы по предмету {selectedSubject}</h2>
            <List
                itemLayout="horizontal"
                dataSource={reviews}
                renderItem={(review) => (
                    <List.Item>
                        <List.Item.Meta
                            avatar={<Avatar>{review.name.charAt(0)}</Avatar>}
                            title={review.name}
                            description={review.text}
                        />
                    </List.Item>
                )}
            />
        </div>
    );
};

export default ReviewList;
