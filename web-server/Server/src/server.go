package main
 
import (
    "database/sql"
    "fmt"
    "strings"
    "strconv"
    _ "github.com/lib/pq"
    zmq "github.com/pebbe/zmq4"
)
type Product struct{
    id int;
    product_type string;
    color string;
    name string;
    additional string;
}

func (product Product) printProduct() string{
    return fmt.Sprintf("Type: %s, number: %d --> Name: %s, color: %s, %s", product.product_type, product.id, product.name, product.color, product.additional)
}

const (
    host     = "database"
    port     = 5432
    user     = "distribuidos"
    password = "javeriana"
    dbname   = "distribuidos" //Like user
    
    backend_url="load-balancer"
    backend_port=30216
)
 
func main() {
    context, _ := zmq.NewContext()
	// Socket to talk to clients
	responder, _ := context.NewSocket(zmq.REP)
	defer responder.Close()
    responder.Connect(fmt.Sprintf("tcp://%s:%d", backend_url, backend_port))
    // connection string
    psqlconn := fmt.Sprintf("host=%s port=%d user=%s password=%s dbname=%s sslmode=disable", host, port, user, password, dbname)
    // open database
    db, err := sql.Open("postgres", psqlconn)
    // close database at end
    defer db.Close()
    if (!CheckError(err)){
        if (!checkConnectionToDB(db)){
            for {
                //  Wait for next request from client
                request, _ := responder.Recv(0)
                // Send reply back to client
                response := processRequest(request, db)
                fmt.Printf("Received request\n%s\nAnd sending response \n%s\n\n", request, response)
                responder.Send(response, 0)
            }
        }
    }
}

func processRequest(request string, db *sql.DB) string{
    full:=""
    if (request=="help"){
        full=menu()
    }else if(request=="consult"){
        full=printProducts(db)
    }else{
        split_request := strings.SplitN(request, ",", 3)
        if (len(split_request)!=3){
            full = "Please try again, send 'help' for help"
        }else{
            username := split_request[0]
            password := split_request[1]
            id, err := strconv.Atoi(split_request[2])
            if (id == 0 || CheckError(err)){
                full = "Please try again, send 'help' for help"
            }else{
                full = buyProduct(db, id, checkLogin(db, username, password))
            }
        }
        
        // Trying to buy a product
        // fmt.Println(buyProduct(db, 2, checkLogin(db, "eruiz", "test3")))
    }
    return full
}

func menu() string{
    return "To see the available products, send 'consult'\nTo buy a product, send '<username>,<password>,<product_number>'\nTo see help, send 'help'"
}

func consult(db *sql.DB) string{
    var full string="\n"
    full+=printProducts(db)
    full+="\n"
    return full
}

func buyProduct(db *sql.DB, product_ID int, customer_ID string) string{
    var bought string="Please try again, this product appears to be already purchased."
    var id int
    rows, err := db.Query(fmt.Sprintf("SELECT ID FROM Products where Owner_ID IS NULL AND ID=%d", product_ID)) //If Owner_ID is null, then it is available to be purchased
    if (!CheckError(err)){
        defer rows.Close()
        for rows.Next() {
            err = rows.Scan(&id)
            CheckError(err)
        }
    }
    if (id==product_ID){
        bought="You succesfully purchased the product!"
        _, err := db.Query(fmt.Sprintf("UPDATE Products SET Owner_ID='%s' WHERE ID=%d", customer_ID, product_ID))
        if (CheckError(err)){
            bought="A problem ocurred when purchasing, please try again..."
        }
    }
    return bought
}

func printProducts(db *sql.DB) string{
    var full string //Store all
    rows, err := db.Query(`SELECT ID, color, product_name, product_type, additional FROM Products where Owner_ID IS NULL`) //If Owner_ID is null, then it is available to be purchased
    defer rows.Close()
    if (!CheckError(err)){
        //Columns
        var id int
        var name string
        var color string
        var product_type string
        var additional string
        // To store the string to print
        var product Product
        //    
        full+="--------\n"
        full+="Products\n"
        full+="--------\n"
        for rows.Next() {
            err = rows.Scan(&id, &color, &name, &product_type, &additional)
            CheckError(err)
            if (!CheckError(err)){
                product=Product{id, product_type, color, name, additional}
                full+=product.printProduct()+"\n"
            }else{
                full+="There was an error, please try again\n"
            }
        }
    }
    return full
}

func checkLogin(db *sql.DB, username string, password string) string{
    statement:=fmt.Sprintf("SELECT ID FROM Customers where username='%s' AND password=crypt('%s', password)", username, password)
    rows, err := db.Query(statement)
    var full string
    defer rows.Close()
    if (!CheckError(err)){
        for rows.Next() {
            err = rows.Scan(&full)
            CheckError(err)
        }
    }
    return full
}

func checkConnectionToDB(db *sql.DB) bool{
    // check db
    err := db.Ping()
    return CheckError(err)
}
 
func CheckError(err error) bool{
    /*if (err!=nil){
        panic(err)
    }*/
    return err!=nil
}