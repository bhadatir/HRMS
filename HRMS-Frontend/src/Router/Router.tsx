import { createBrowserRouter } from "react-router-dom";

export const router = createBrowserRouter([
    // {
    //     path:"/",
    //     element:<App />,
    //     children: [
           
    //     ],
    // },
    // {
    //     path:"/about",
    //     element: (
    //         <>
    //             <ShopNavbar />
    //             <About />
    //         </>
    //     ),
    // },
    {
        path:"/*",
        element:<h1>404 Not Found</h1>,
    }
])

