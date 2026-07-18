package com.widoo.pitlane.data.local

object VehicleCatalog {

    val brands = listOf(
        "Chevrolet",
        "Citroën",
        "Fiat",
        "Ford",
        "Honda",
        "Hyundai",
        "Jeep",
        "Kia",
        "Mercedes-Benz",
        "Nissan",
        "Peugeot",
        "Renault",
        "Suzuki",
        "Toyota",
        "Volkswagen",
        "Otra"
    )

    val modelsByBrand = mapOf(
        "Chevrolet" to listOf(
            "Agile", "Captiva", "Cavalier", "Classic", "Cobalt",
            "Corsa", "Cruze", "Equinox", "Montana", "Onix",
            "Onix Plus", "Prisma", "S10", "Spin", "Tracker", "Trailblazer", "Trax"
        ),
        "Citroën" to listOf(
            "Berlingo", "C3", "C3 Aircross", "C4", "C4 Cactus",
            "C4 Lounge", "C5", "DS3", "DS4", "DS5", "Jumpy", "Xsara Picasso"
        ),
        "Fiat" to listOf(
            "Argo", "Bravo", "Cronos", "Doblò", "Ducato",
            "Fiorino", "Grand Siena", "Linea", "Mobi", "Palio",
            "Punto", "Siena", "Strada", "Toro", "Uno"
        ),
        "Ford" to listOf(
            "Bronco", "EcoSport", "Edge", "Escape", "Expedition",
            "Explorer", "F-150", "Fiesta", "Focus", "Fusion",
            "Ka", "Kuga", "Maverick", "Mondeo", "Mustang",
            "Ranger", "Territory"
        ),
        "Honda" to listOf(
            "Accord", "City", "Civic", "CR-V", "Fit",
            "HR-V", "Jazz", "Odyssey", "Pilot", "WR-V"
        ),
        "Hyundai" to listOf(
            "Accent", "Creta", "Elantra", "Galloper", "Getz",
            "i10", "i20", "i25", "i30", "ix35",
            "Santa Fe", "Sonata", "Terracan", "Tucson", "Venue"
        ),
        "Jeep" to listOf(
            "Cherokee", "Compass", "Grand Cherokee", "Gladiator",
            "Renegade", "Wrangler"
        ),
        "Kia" to listOf(
            "Carnival", "Cerato", "K5", "Niro", "Picanto",
            "Rio", "Seltos", "Sorento", "Soul", "Sportage",
            "Stinger", "Telluride"
        ),
        "Mercedes-Benz" to listOf(
            "Clase A", "Clase B", "Clase C", "Clase E", "Clase S",
            "CLA", "GLA", "GLB", "GLC", "GLE",
            "GLS", "ML", "Sprinter", "Vito"
        ),
        "Nissan" to listOf(
            "Frontier", "Kicks", "Leaf", "March", "Murano",
            "Pathfinder", "Sentra", "Tiida", "Versa", "X-Trail"
        ),
        "Peugeot" to listOf(
            "2008", "208", "3008", "301", "307",
            "308", "408", "5008", "Partner", "Rifter"
        ),
        "Renault" to listOf(
            "Captur", "Clio", "Duster", "Fluence", "Kangoo",
            "Koleos", "Kwid", "Logan", "Master", "Megane",
            "Oroch", "Sandero", "Stepway", "Symbol"
        ),
        "Suzuki" to listOf(
            "Alto", "Baleno", "Celerio", "Ciaz", "Dzire",
            "Ertiga", "Grand Vitara", "Ignis", "Jimny",
            "S-Cross", "Swift", "Vitara"
        ),
        "Toyota" to listOf(
            "Camry", "Corolla", "Corolla Cross", "Etios", "GR86",
            "Highlander", "Hilux", "Land Cruiser", "Prado",
            "RAV4", "Rush", "SW4", "Yaris"
        ),
        "Volkswagen" to listOf(
            "Amarok", "Caddy", "Fox", "Gol", "Golf",
            "Jetta", "Passat", "Polo", "Saveiro", "Suran",
            "T-Cross", "Taos", "Tiguan", "Touareg", "Vento",
            "Virtus", "Voyage"
        ),
        "Otra" to listOf("Otro modelo")
    )

    fun getModels(brand: String): List<String> {
        return modelsByBrand[brand] ?: listOf("Otro modelo")
    }
}