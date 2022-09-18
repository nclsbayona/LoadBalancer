package main
 
import (
    "os"
    "os/exec"
    "runtime"
    "database/sql"
    "fmt"
    "syscall"
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

type Server struct {
    db_host string;
    db_port int;
    db_user string;
    db_password string;
    db_dbname string;
    db_type string;
    db *sql.DB;
    backend_socket_url string;
    backend_socket_port string;
    context *zmq.Context;
    responder *zmq.Socket;
}

func (server *Server) Init (db_host string, db_port int, db_user string, db_password string, db_dbname string, db_type string, backend_socket_url string, backend_socket_port int) {
    context, err:=zmq.NewContext();
    if (!CheckError(err)){
        server.context=context;
        // Socket to talk to clients
	    responder, err := context.NewSocket(zmq.REP)
        if (!CheckError(err)){
            server.responder=responder
            server.responder.Connect(fmt.Sprintf("tcp://%s:%d", backend_socket_url, backend_socket_port))
            // open database
            db, err := sql.Open(db_type, fmt.Sprintf("host=%s port=%d user=%s password=%s dbname=%s sslmode=disable",db_host, db_port, db_user, db_password, db_dbname))
            if (!CheckError(err)){
                server.db=db
                if (server.checkConnectionToDB()){
                    RestartProcess(err)
                }
            }else{
                RestartProcess(err)
            }
        }else{
            RestartProcess(err)
        }
    }else{
        RestartProcess(err);
    }
}

func (server *Server) attend (){
    for {
        //  Wait for next request from client
        request, err := server.responder.Recv(0)
        // Send reply back to client
        if (!CheckError(err)){
            response := server.processRequest(request)
            fmt.Printf("Received request\n%s\nAnd sending response \n%s\n\n", request, response)
            server.responder.Send(response, 0)
        }
    }
}

const (
    host     = "localhost"
    port     = 5432
    user     = "distribuidos"
    password = "javeriana"
    dbname   = "distribuidos" //Like user
    
    backend_url="localhost"
    backend_port=30216
)
 
func main() {
    /*
    context, err := zmq.NewContext()
	// Socket to talk to clients
	responder, err := context.NewSocket(zmq.REP)
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
    */
    server := new(Server)
    server.Init(host, port, user, password, dbname, "postgres", backend_url, backend_port)
    fmt.Println("Ready...")
    server.attend()
}

func (server *Server) processRequest(request string) string{
    full:=""
    if (request=="help"){
        full=server.menu()
    }else if(request=="consult"){
        full=server.printProducts()
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
                full = server.buyProduct(id, server.checkLogin(username, password))
            }
        }
    }
    return full
}

func (server *Server) menu() string{
    return "To see the available products, send 'consult'\nTo buy a product, send '<username>,<password>,<product_number>'\nTo see help, send 'help'"
}

func (server *Server) consult() string{
    var full string="\n"
    full+=server.printProducts()
    full+="\n"
    return full
}

func (server *Server) buyProduct(product_ID int, customer_ID string) string{
    var bought string="Please try again, this product appears to be already purchased."
    var id int
    rows, err := server.db.Query(fmt.Sprintf("SELECT ID FROM Products where Owner_ID IS NULL AND ID=%d", product_ID)) //If Owner_ID is null, then it is available to be purchased
    if (!CheckError(err)){
        defer rows.Close()
        for rows.Next() {
            err = rows.Scan(&id)
            CheckError(err)
        }
    }
    if (id==product_ID){
        bought="You succesfully purchased the product!"
        _, err := server.db.Query(fmt.Sprintf("UPDATE Products SET Owner_ID='%s' WHERE ID=%d", customer_ID, product_ID))
        if (CheckError(err)){
            bought="A problem ocurred when purchasing, please try again..."
        }
    }
    return bought
}

func (server *Server) printProducts() string{
    var full string //Store all
    rows, err := server.db.Query(`SELECT ID, color, product_name, product_type, additional FROM Products where Owner_ID IS NULL`) //If Owner_ID is null, then it is available to be purchased
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

func (server *Server) checkLogin(username string, password string) string{
    statement:=fmt.Sprintf("SELECT ID FROM Customers where username='%s' AND password=crypt('%s', password)", username, password)
    rows, err := server.db.Query(statement)
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

func (server *Server) checkConnectionToDB() bool{
    // check db
    err := server.db.Ping()
    return CheckError(err)
}
 
func CheckError(err error) bool{
    return err!=nil
}

func RestartProcess(err error) error{
    fmt.Println("Restarting process...")
    fmt.Println(err)
    self, err := os.Executable()
    if err != nil {
        return err
    }
    args := os.Args
    env := os.Environ()
    // Windows does not support exec syscall.
    if runtime.GOOS == "windows" {
        cmd := exec.Command(self, args[1:]...)
        cmd.Stdout = os.Stdout
        cmd.Stderr = os.Stderr
        cmd.Stdin = os.Stdin
        cmd.Env = env
        err := cmd.Run()
        if err == nil {
            os.Exit(0)
        }
        return err
    }
    return syscall.Exec(self, args, env)
}