// CourseList.jsx
import React, {useEffect, useState} from 'react';
import {Button} from 'antd';
import SubjectList from "../subjectChose/subject";
import PostService from '../postService/PostService';
import "./course.css";

const CourseList = ({university, selectedCourse, setSelectedCourse}: any) => {
    const [courses, setCourses] = useState([]);

    useEffect(() => {
        // Загрузка списка курсов для выбранного университета
        PostService.getCourses(university.name).then((response: any) => {
            const sortedCourses = response.data.sort((a: any, b: any) => a.number - b.number);
            setCourses(sortedCourses);
        });
    }, [university]);

    const handleCourseClick = (course: any) => {
        setSelectedCourse(course.number);
    };

    return (
        <div>
            <header className="course-header">Выберите курс для {university.name}</header>
            <div className="course-buttons-container">
                {courses.map((course: any, index) => (
                    <Button
                        key={index}
                        type={selectedCourse === course ? 'primary' : 'default'}
                        onClick={() => handleCourseClick(course)}
                        className="course-button">
                        {course.number}
                    </Button>
                ))}
            </div>
            {selectedCourse && <SubjectList university={university} course={selectedCourse}/>}
        </div>
    );
};

export default CourseList;
