import React from 'react';
import {BrowserRouter, BrowserRouter as Router, Route, Switch} from 'react-router-dom';
import UniversityPage from './universityChose/university';
import './App.css';
import BookListPage from "./materialList/MaterislList";
import AuthButtons from "./Auth/AuthButtons";
import MainMenu from "./mainMenu/MainMenu";


const App = () => {
    return (
        <div className="App">
            <BrowserRouter>
                <AuthButtons/>
                <MainMenu/>
                <Switch>
                    <Route path="/student_compass/:university/:course/:subject" component={BookListPage}/>
                    <Route path="/" component={UniversityPage}/>
                </Switch>
            </BrowserRouter>
        </div>
    );
}

const Root = () => {
    return (
        <Router basename={process.env.PUBLIC_URL}>
            <App/>
        </Router>
    );
}

export default Root;