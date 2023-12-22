// UniversityPage.jsx
import React, {useEffect, useState} from 'react';
import {Row, Col, Spin} from 'antd';
import {Link} from 'react-router-dom';
import CourseList from '../courseChose/course';
import PostService from '../postService/PostService';
import './university.css';

const UniversityPage: React.FC = () => {
    const [loading, setLoading] = useState(true);
    const [universities, setUniversities] = useState([]);
    const [selectedUniversity, setSelectedUniversity] = useState(null);

    useEffect(() => {
        // Загрузка списка университетов при монтировании компонента
        PostService.getUniversities(1).then((response) => {
            setUniversities(response.data);
            setLoading(false);
        });
    }, []);

    const handleUniversityClick = (university: any) => {
        setLoading(true);
        setSelectedUniversity(university);
    };

    return (
        <div>
            <header className="university-header">Выберите университет</header>
            {loading ? (
                <Spin size="large"/>
            ) : (
                <Row gutter={[48, 16]} justify="center" align="middle">
                    {universities.map((university, index) => (
                        <Col key={index} xs={24} sm={12} md={8} lg={4}>
                            <div onClick={() => handleUniversityClick(university)}>
                                <Link to={university.link}>
                                    <img src={university.imageUrl} alt={university.name} className="icon"/>
                                </Link>
                            </div>
                        </Col>
                    ))}
                </Row>
            )}
            {selectedUniversity && <CourseList university={selectedUniversity}/>}
        </div>
    );
};

export default UniversityPage;
