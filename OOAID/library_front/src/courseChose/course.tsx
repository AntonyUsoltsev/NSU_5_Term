// CourseList.jsx
import React, { useEffect, useState } from 'react';
import { Button, Spin } from 'antd';
import SubjectList from "../subjectChose/subject";
import PostService from '../postService/PostService';
import "./course.css";

const CourseList = ({ university }) => {
    const [loading, setLoading] = useState(true);
    const [courses, setCourses] = useState([]);
    const [selectedCourse, setSelectedCourse] = useState(null);

    useEffect(() => {
        // Загрузка списка курсов для выбранного университета
        PostService.getCourses(university.name).then((response) => {
            setCourses(response.data);
            setLoading(false);
        });
    }, [university]);

    const handleCourseClick = (course: any) => {
        setLoading(true);
        setSelectedCourse(course);
    };

    return (
        <div>
            <header className="course-header">Выберите курс для {university.name}</header>
            {loading ? (
                <Spin size="large" />
            ) : (
                <div className="course-buttons-container">
                    {courses.map((course, index) => (
                        <Button
                            key={index}
                            type={selectedCourse === course ? 'primary' : 'default'}
                            onClick={() => handleCourseClick(course)}
                            className="course-button">
                            {course.name}
                        </Button>
                    ))}
                </div>
            )}
            {selectedCourse && <SubjectList university={university} course={selectedCourse} />}
        </div>
    );
};

export default CourseList;
