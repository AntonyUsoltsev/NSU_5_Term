import axios from "axios";

export default class PostService {
    static errorHandler(error: any) {
        if (error.response) {
            console.error(error.response.data);
            console.error(error.response.status);
            console.error(error.response.headers);
        } else if (error.request) {
            console.error(error.request);
        } else {
            console.error('Error', error.message);
        }
    }

    static async getUniversities(tableId: any) {
        console.log("tableId" + tableId)
        try {
            const value = await axios.get("http://localhost:8080/student_compass");
            console.log(value)
            return value;
        } catch (error) {
            this.errorHandler(error);
        }
    }

    static async getCourses(universityName: any) {
        console.log(`http://localhost:8080/student_compass/${universityName}`)
        try {
            const value = await axios.get(`http://localhost:8080/student_compass/${universityName}`);
            console.log(value)
            return value;
        } catch (error) {
            this.errorHandler(error);
        }
    }

    static async getSubjects(universityName: string, courseValue: string) {
        console.log(`http://localhost:8080/student_compass/${universityName}/${courseValue}`)
        try {
            const value = await axios.get(`http://localhost:8080/student_compass/${universityName}/${courseValue}`);
            console.log(value)
            return value;
        } catch (error) {
            this.errorHandler(error);
        }
    }

    static async getBooks(universityName: any, courseValue: any, selectedSubject: any) {
        console.log(`http://localhost:8080/student_compass/${universityName}/${courseValue}/${selectedSubject}`)
        try {
            const value = await axios.get(`http://localhost:8080/student_compass/${universityName}/${courseValue}/${selectedSubject}`);
            console.log(value)
            return value;
        } catch (error) {
            this.errorHandler(error);
        }
    }
    static async getReviews(universityName: any, courseValue: any, selectedSubject: any) {
        try {
            const value = await axios.get(`http://localhost:8080/student_compass/${universityName}/${courseValue}/${selectedSubject}/reviews`);
            console.log(value)
            return value;
        } catch (error) {
            this.errorHandler(error);
        }
    }
    static async postReview(){

    }
}