import withMT from "@material-tailwind/html/utils/withMT";
module.exports = withMT({
  prefix: "picture-bed-",
  content: ["./index.html", "./src/**/*.{vue,js,ts,jsx,tsx}"],
  theme: {
    extend: {},
  },
  plugins: [],
});
