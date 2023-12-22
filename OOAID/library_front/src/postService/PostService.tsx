import axios from "axios";

export default class PostService {
    static errorHandler(error) {
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

    static async getUniversities(tableId) {
        const data = {key: "value"};
        console.log("tableId" + tableId)
        try {
            const value = await axios.get("http://localhost:8080/student_compass", {params: data});
            console.log(value)
            return value;
        } catch (error) {
            this.errorHandler(error);
        }
    }

    static async getCourses(universityName: string) {
        const data = {key: "value"};
        try {
            const value = await axios.get("http://localhost:8080/student_compass/" + universityName, {params: data});
            console.log(value)
            return value;
        } catch (error) {
            this.errorHandler(error);
        }
    }

    static async getSubjects(universityName: string, courseValue: string) {
        const data = {key: "value"};
        try {
            const value = await axios.get("http://localhost:8080/student_compass/" +
                universityName + "/" +
                courseValue, {params: data});
            console.log(value)
            return value;
        } catch (error) {
            this.errorHandler(error);
        }
    }

    static async getMaterials(universityName: string, courseValue: string, subject: string) {
        const data = {key: "value"};
        try {
            const value = await axios.get("http://localhost:8080/student_compass/" +
                universityName + "/" +
                courseValue + "/" +
                subject, {params: data});

            console.log(value)
            return value;
        } catch (error) {
            this.errorHandler(error);
        }
    }

    static async GetMainMenu() {
        const data = {key: "value"};
        try {
            return await axios.get("http://localhost:8080/backend/restaurant", {params: data});
        } catch (error) {
            this.errorHandler(error);
        }
    }

    static async GetCategoryDishes(id, title) {
        console.log(id[1])
        console.log(title)
        try {
            return await axios.post("http://localhost:8080/backend/restaurant/category", {
                id: Number(id === null ? null : id[1]),
                title: title
            });
        } catch (error) {
            this.errorHandler(error);
        }
    }

    static async GetDishInfo(id, title) {
        console.log(id)
        try {
            return await axios.post("http://localhost:8080/backend/restaurant/dish", {
                    id: Number(id),
                    title:
                    title
                }
            )
                ;
        } catch (error) {
            this.errorHandler(error);
        }
    }

    static async AddDishToCart(dishId, title, tableId, count) {
        console.log(dishId)
        console.log(tableId)
        try {
            return await axios.post("http://localhost:8080/backend/restaurant/order", {
                    dishFindDto: {
                        id: dishId, title:
                        title
                    }
                    ,
                    count: count,
                    numberTable:
                    tableId,
                }
            )
                ;
        } catch
            (error) {
            this.errorHandler(error);
        }
    }

    static async ChangeDishCount(dishId, title, tableId, count) {
        console.log(dishId)
        console.log(tableId)
        try {
            return await axios.post("http://localhost:8080/backend/restaurant/order/change", {
                    dishFindDto: {
                        id: dishId, title:
                        title
                    }
                    ,
                    count: count,
                    numberTable:
                    tableId,
                }
            )
                ;
        } catch
            (error) {
            this.errorHandler(error);
        }
    }

    static async deleteDishCount(dishId, title, tableId) {
        console.log(dishId);
        console.log(tableId);
        try {
            const response = await axios.delete('http://localhost:8080/backend/restaurant/cart', {
                data: {
                    dishFindDto: {id: dishId, title: title},
                    numberTable: tableId
                },
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            return response.data;
        } catch (error) {
            this.errorHandler(error);
        }
    }

    static async payCheck(listOrderDto, tableId, cost) {
        console.log(listOrderDto)
        console.log(tableId)
        console.log(cost)
        try {
            return await axios.post("http://localhost:8080/backend/restaurant/cart/payment", {
                    listOrderDto: listOrderDto,
                    numberTable:
                    tableId,
                    cost:
                    cost
                }
            )
                ;
        } catch
            (error) {
            this.errorHandler(error);
        }
    }


    static async GetReviews() {
        const data = {key: "value"};
        try {
            const value = await axios.get("http://localhost:8080/backend/restaurant/reviews", {params: data});
            console.log(value)
            return value
        } catch (error) {
            this.errorHandler(error);
        }
    }


    static async AddReview(score, review) {
        try {
            return await axios.post("http://localhost:8080/backend/restaurant/review", {
                    score: score,
                    text:
                    review,
                }
            )
                ;
        } catch
            (error) {
            this.errorHandler(error);
        }
    }
}