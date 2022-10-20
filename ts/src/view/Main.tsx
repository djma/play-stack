import "./Main.css";

import * as React from "react";
import { useMemo } from "react";

export default function Main() {
  return (
    <main>
      <HelloWorldDisplay incrementBy={1} />
    </main>
  );
}

interface Props {
  incrementBy: number;
}
export class HelloWorldDisplay extends React.PureComponent<Props> {
  state = { counter: 0 };

  constructor(props: Props) {
    super(props);
  }

  componentDidMount() {
    setTimeout(this.reload, 500);
    setInterval(this.reload, 5000);
  }

  handleSelect(event: React.ChangeEvent<HTMLSelectElement>) {
    console.log(event.target.value);
  }

  handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    console.log("submit hodl");
  };

  reload = async () => {
    this.setState({ counter: this.state.counter + this.props.incrementBy });
  };

  render() {
    return (
      <main>
        <h1>This is H1</h1>
        <div>This is a div</div>
        <ul>
          <li>List item 1</li>
          <li>List item 2</li>
        </ul>
        <br></br>
        <label>This is a label</label>
        <select name="selectList" id="selectList" onChange={this.handleSelect}>
          <option value="1">A</option>
          <option value="2">B</option>
        </select>
        <form onSubmit={this.handleSubmit}>
          <label>
            This is another label:
            <input type="text" name="name" />
          </label>
          <input type="submit" value="ButtonLabel" />
        </form>
        <br></br>
        <h2>Counter: {this.state.counter}</h2>
      </main>
    );
  }
}
