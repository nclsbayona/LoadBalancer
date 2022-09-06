package main
 
import (
    "database/sql"
    "fmt"
    _ "github.com/lib/pq"
)
 
const (
    host     = "localhost"
    port     = 5432
    user     = "distribuidos"
    password = "javeriana"
    dbname   = "distribuidos" //Like user
)
 
func main() {
    // connection string
    psqlconn := fmt.Sprintf("host=%s port=%d user=%s password=%s dbname=%s sslmode=disable", host, port, user, password, dbname)
    // open database
    db, err := sql.Open("postgres", psqlconn)
    CheckError(err)
    // close database
    defer db.Close()
    checkConnectionToDB(db)
    fmt.Println(consult(db))
}

func consult(db *sql.DB) string{
    var full string="\n"
    full+=printShoes(db)
    full+="\n"
    full+=printElectronics(db)
    full+="\n"
    return full
}

func printShoes(db *sql.DB) string{
    var full string //Store all
    rows, err := db.Query(`SELECT * FROM Shoes`)
    CheckError(err)
    //Columns
    var id int
    var name string
    var color string
    var shoe_type string
    // To store the string to print
    var shoe string
    //    
    full+="------\n"
    full+="Shoes\n"
    full+="------\n"
    defer rows.Close()
    for rows.Next() {
        err = rows.Scan(&id, &color, &name, &shoe_type)
        CheckError(err)
        shoe=fmt.Sprintf("Shoe %d --> Name: %s, color: %s, type: %s", id, name, color, shoe_type)
        full+=shoe+"\n"
    }
    return full
}

func printElectronics(db *sql.DB) string{
    var full string //Store all
    rows, err := db.Query(`SELECT * FROM Electronics`)
    CheckError(err)
    //Columns
    var id int
    var name string
    var color string
    var weight float32
    // To store the string to print
    var electronic string
    //    
    full+="-----------\n"
    full+="Electronics\n"
    full+="-----------\n"
    defer rows.Close()
    for rows.Next() {
        err = rows.Scan(&id, &color, &name, &weight)
        CheckError(err)
        electronic=fmt.Sprintf("Electronic %d --> Name: %s, color: %s, weight: %fkg", id, name, color, weight)
        full+=electronic+"\n"
    }
    return full
}

func checkConnectionToDB(db *sql.DB){
    // check db
    err := db.Ping()
    CheckError(err)
}
 
func CheckError(err error) {
    if err != nil {
        panic(err)
    }
}