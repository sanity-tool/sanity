func testVarLet() {
    var myVariable = 42
    myVariable = 50
    let myConstant = 42
}

func testImplicitExplicit() {
    let implicitInteger = 70
    let implicitDouble = 70.0
    let explicitDouble: Double = 70
}

func testString() {
    let label = "The width is "
    let width = 94
    let widthLabel = label + String(width)

    let apples = 3
    let oranges = 5
    let appleSummary = "I have \(apples) apples."
    let fruitSummary = "I have \(apples + oranges) pieces of fruit."
}

func testArraysDictionaries() {
    var shoppingList = ["catfish", "water", "tulips", "blue paint"]
    shoppingList[1] = "bottle of water"

    var occupations = [
        "Malcolm": "Captain",
        "Kaylee": "Mechanic",
    ]
    occupations["Jayne"] = "Public Relations"

    let emptyArray = [String]()
    let emptyDictionary = [String: Float]()

    shoppingList = []
    occupations = [:]
}
