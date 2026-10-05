package com.example.hipermarketsallingapplication.ui.auth

data class CountryInfo(
    val name: String,
    val currency: String,
    val cities: List<String>
)

object LocationData {
    val countries = listOf(
        CountryInfo("United States", "USD", listOf("New York", "Los Angeles", "Chicago", "Houston", "Miami", "San Francisco", "Seattle", "Boston", "Dallas")),
        CountryInfo("Canada", "CAD", listOf("Toronto", "Vancouver", "Montreal", "Calgary", "Ottawa", "Edmonton", "Quebec City")),
        CountryInfo("Mexico", "MXN", listOf("Mexico City", "Guadalajara", "Monterrey", "Puebla", "Cancun", "Tijuana")),
        CountryInfo("Brazil", "BRL", listOf("São Paulo", "Rio de Janeiro", "Brasília", "Salvador", "Fortaleza", "Belo Horizonte")),
        CountryInfo("Argentina", "ARS", listOf("Buenos Aires", "Córdoba", "Rosario", "Mendoza", "La Plata")),
        CountryInfo("Colombia", "COP", listOf("Bogotá", "Medellín", "Cali", "Barranquilla", "Cartagena")),
        CountryInfo("Chile", "CLP", listOf("Santiago", "Valparaíso", "Concepción", "Antofagasta")),
        CountryInfo("Peru", "PEN", listOf("Lima", "Arequipa", "Trujillo", "Cusco")),

        CountryInfo("United Kingdom", "GBP", listOf("London", "Manchester", "Birmingham", "Glasgow", "Liverpool", "Edinburgh", "Bristol")),
        CountryInfo("France", "EUR", listOf("Paris", "Marseille", "Lyon", "Toulouse", "Nice", "Bordeaux", "Strasbourg")),
        CountryInfo("Germany", "EUR", listOf("Berlin", "Munich", "Frankfurt", "Hamburg", "Cologne", "Stuttgart", "Düsseldorf")),
        CountryInfo("Spain", "EUR", listOf("Madrid", "Barcelona", "Valencia", "Seville", "Bilbao", "Malaga", "Zaragoza")),
        CountryInfo("Italy", "EUR", listOf("Rome", "Milan", "Naples", "Turin", "Palermo", "Florence", "Bologna")),
        CountryInfo("Netherlands", "EUR", listOf("Amsterdam", "Rotterdam", "The Hague", "Utrecht", "Eindhoven")),
        CountryInfo("Switzerland", "CHF", listOf("Zurich", "Geneva", "Basel", "Lausanne", "Bern")),
        CountryInfo("Sweden", "SEK", listOf("Stockholm", "Gothenburg", "Malmö", "Uppsala")),
        CountryInfo("Poland", "PLN", listOf("Warsaw", "Kraków", "Łódź", "Wrocław", "Poznań")),
        CountryInfo("Portugal", "EUR", listOf("Lisbon", "Porto", "Coimbra", "Braga")),
        CountryInfo("Belgium", "EUR", listOf("Brussels", "Antwerp", "Ghent", "Bruges")),
        CountryInfo("Austria", "EUR", listOf("Vienna", "Salzburg", "Innsbruck", "Graz")),
        CountryInfo("Ireland", "EUR", listOf("Dublin", "Cork", "Galway", "Limerick")),
        CountryInfo("Norway", "NOK", listOf("Oslo", "Bergen", "Trondheim", "Stavanger")),
        CountryInfo("Denmark", "DKK", listOf("Copenhagen", "Aarhus", "Odense", "Aalborg")),
        CountryInfo("Finland", "EUR", listOf("Helsinki", "Espoo", "Tampere", "Turku")),
        CountryInfo("Greece", "EUR", listOf("Athens", "Thessaloniki", "Patras", "Heraklion")),
        CountryInfo("Turkey", "TRY", listOf("Istanbul", "Ankara", "İzmir", "Bursa", "Antalya")),
        CountryInfo("Russia", "RUB", listOf("Moscow", "Saint Petersburg", "Novosibirsk", "Yekaterinburg", "Kazan")),
        CountryInfo("Ukraine", "UAH", listOf("Kyiv", "Kharkiv", "Odesa", "Dnipro", "Lviv")),

        CountryInfo("China", "CNY", listOf("Beijing", "Shanghai", "Shenzhen", "Guangzhou", "Chengdu", "Hangzhou", "Wuhan")),
        CountryInfo("Japan", "JPY", listOf("Tokyo", "Osaka", "Kyoto", "Yokohama", "Nagoya", "Sapporo", "Fukuoka")),
        CountryInfo("India", "INR", listOf("New Delhi", "Mumbai", "Bengaluru", "Chennai", "Kolkata", "Hyderabad", "Pune")),
        CountryInfo("South Korea", "KRW", listOf("Seoul", "Busan", "Incheon", "Daegu", "Daejeon")),
        CountryInfo("Indonesia", "IDR", listOf("Jakarta", "Surabaya", "Bandung", "Medan", "Bali")),
        CountryInfo("Vietnam", "VND", listOf("Hồ Chí Minh City", "Hanoi", "Da Nang", "Hai Phong")),
        CountryInfo("Thailand", "THB", listOf("Bangkok", "Chiang Mai", "Phuket", "Pattaya")),
        CountryInfo("Philippines", "PHP", listOf("Manila", "Cebu City", "Davao City", "Quezon City")),
        CountryInfo("Malaysia", "MYR", listOf("Kuala Lumpur", "Penang", "Johor Bahru", "Malacca")),
        CountryInfo("Singapore", "SGD", listOf("Singapore")),
        CountryInfo("Pakistan", "PKR", listOf("Karachi", "Lahore", "Islamabad", "Faisalabad", "Rawalpindi")),
        CountryInfo("Bangladesh", "BDT", listOf("Dhaka", "Chittagong", "Sylhet", "Rajshahi")),

        CountryInfo("United Arab Emirates", "AED", listOf("Dubai", "Abu Dhabi", "Sharjah", "Al Ain", "Ajman")),
        CountryInfo("Saudi Arabia", "SAR", listOf("Riyadh", "Jeddah", "Mecca", "Medina", "Dammam")),
        CountryInfo("Qatar", "QAR", listOf("Doha", "Al Rayyan", "Al Wakrah")),
        CountryInfo("Egypt", "EGP", listOf("Cairo", "Alexandria", "Giza", "Luxor", "Aswan")),
        CountryInfo("Morocco", "MAD", listOf("Casablanca", "Rabat", "Marrakech", "Fez", "Tangier", "Agadir")),
        CountryInfo("Algeria", "DZD", listOf("Algiers", "Oran", "Constantine", "Annaba")),
        CountryInfo("Tunisia", "TND", listOf("Tunis", "Sfax", "Sousse", "Bizerte")),
        CountryInfo("South Africa", "ZAR", listOf("Johannesburg", "Cape Town", "Durban", "Pretoria", "Port Elizabeth")),
        CountryInfo("Nigeria", "NGN", listOf("Lagos", "Abuja", "Port Harcourt", "Kano", "Ibadan")),
        CountryInfo("Kenya", "KES", listOf("Nairobi", "Mombasa", "Kisumu", "Nakuru")),

        CountryInfo("Australia", "AUD", listOf("Sydney", "Melbourne", "Brisbane", "Perth", "Adelaide", "Canberra")),
        CountryInfo("New Zealand", "NZD", listOf("Auckland", "Wellington", "Christchurch", "Hamilton"))
    )

    fun getCities(countryName: String): List<String> {
        return countries.find { it.name.equals(countryName, true) }?.cities ?: listOf("Capital / Major City")
    }

    fun getCurrency(countryName: String): String {
        return countries.find { it.name.equals(countryName, true) }?.currency ?: "USD"
    }
}
