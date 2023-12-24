import React from 'react';
import {BrowserRouter as Router, Route, Switch} from 'react-router-dom';
import UniversityPage from './universityChose/university';
import './App.css';
import BookListPage from "./materialList/MaterislList";
import AuthButtons from "./Auth/AuthButtons";


const App = () => {
    return (
        <div className="App">
            <Router>
                <AuthButtons />
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
        <Router basename={process.env.PUBLIC_URL}>
            <App/>
        </Router>
    );
}

export default Root;