import React from 'react';
import {BrowserRouter as Router, Route} from 'react-router-dom';
import UniversityPage from './universityChose/university';
import './App.css';
import {Switch} from "antd";
import BookListPage from "./materialList/MaterislList";
import AuthenticationForm from "./authorizitaion/AuthPage";


const App = () => {
    return (
        <div className="App">
            <Router>
                <AuthenticationForm />
                <Switch>
                    <Route path="/student_compass/:university/:course/:subject" component={BookListPage} />
                    <Route path="/" component={UniversityPage}/>
                </Switch>
            </Router>
        </div>
    );
}

const Root = () => {
    return (
        <Router>
            <App/>
        </Router>
    );
}

export default Root;