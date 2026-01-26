
const lbToKg = (lb) => {
  return lb * 0.4535924
}

const kgToLb = (kg) => {
  return kg / 0.4535924
}


const state = new Proxy(
  { state: {
      reps: 11,
      weight: 25,
      weightMin: 5,
      unit: 'lb',
      minRepsToConsider: 5,
      maxRepsToConsider: 16,
      consider: {down: 15, up: 20},
      idealChange: 0.03,
      stdIncrement: 5.0,
      micoIncrement: 5.0
    }
  },
  {
    set(target, key, value) {
      target[key] = value
        console.log(value)
      console.log("rendering!")
      updateDOM(render(value));
      return true
    }
  }
)


const figureItOut = (state) => {
  const current = state.reps * state.weight
  const totalWithExtraRep = (state.reps + 1) * state.weight
  const totalWithExtraWeight = state.reps * (state.weight + state.stdIncrement)

  const percentChangeForExtraRep = (totalWithExtraRep-current) / current
  const percentChangeForExtraWeight = (totalWithExtraWeight-current) / current

  const lowerBound = Math.max(state.weight - state.consider.down, state.weightMin)
  const upperBound = state.weight + state.consider.up

  const repCounts = [...Array(state.maxRepsToConsider - state.minRepsToConsider)
      .keys().map(x => x + state.minRepsToConsider)];


  let rows = [];
  for (let weight = lowerBound; weight < upperBound; weight += state.micoIncrement) {
    let row = [{type: 'weight', value: weight}]
    for (let reps = state.minRepsToConsider; reps < state.maxRepsToConsider; reps++) {
      const thisEffort = reps * weight
      const percentChange = ((thisEffort - current) / current)
      const rounded = percentChange * 100
      row.push({
        type: 'analysis',
        thisEffort: thisEffort,
        percentChange: rounded,
        closeToIdeal: percentChange > 0 && percentChange <= state.idealChange,
        // within 1% in either direction
        almostEquivalentToExtraRep: percentChangeForExtraRep < (percentChange * 1.01) && percentChangeForExtraRep > (percentChange * 0.99),
      })
    }
    rows.push(row);
  }
  return {
    currentEffort: current,
    percentChangeForExtraRep: percentChangeForExtraRep * 100,
    percentChangeForExtraWeight: percentChangeForExtraWeight * 100,
    candidates: {
      header: repCounts,
      body: rows
    }
  }
}


const highlight = (col) => {
    if (col.closeToIdeal) {
        return '#73b270'
    } else if (col.almostEquivalentToExtraRep) {
        return '#9ac1ea'
    } else {
        return 'white';
    }
}

const renderSummary = (state, summary) => (`
    <div>
        <h4 style="margin-bottom: 0">Summary</h4>
        <ul style="list-style: none; margin-top: 0; padding-left: 20px">
            <li>Current Total Work <small>(rep &times; weight)</small>: <span style="font-weight: bold"> ${summary.currentEffort.toFixed(2)} ${state.unit}s</span></li>
            <li>Percent increase for extra rep: <span style="font-weight: bold">${summary.percentChangeForExtraRep.toFixed(2)}%</span></li>
            <li>Precent increase for extra ${state.stdIncrement}${state.unit}: <span style="font-weight: bold">${summary.percentChangeForExtraWeight.toFixed(2)}%</span></li>
        </ul>
    </div>
`)

const renderTable = (state, summary) => {
  return `
    <blockquote style="margin: 16px 0">
        The below table has alternative rep &times; weight schemes for finding smaller progressions
      </blockquote>  
    <table>
      <thead>
        <tr>
          <th></th>
          <th colspan=${summary.candidates.header.length}>Reps</th>
        </tr>
        <tr>
          <th>Weight</th>
          ${summary.candidates.header.map(rep => `
            <th>${rep}</th>
          `).join("")}
        </tr>
      </thead>
      <tbody>
        ${summary.candidates.body.map(row => `
          <tr>
            ${row.map(col => `
              ${col.type === 'weight'
              ? `<td>${col.value} ${state.unit}</td>`
              : `<td style="background-color: ${highlight(col)}">
                    ${col.percentChange.toFixed(2)}%</td>
              `}
            `).join("")}
          </tr>
        `).join("")}
      </tbody>
    </table>
  `
}


const legend = (state) => (`
    <div>
        <h3 style="margin-bottom: 0">Legend</h3>
        <div style="padding-left: 10px">
            <div style="display: flex; align-items: center">
                <div style="width: 12px; height: 12px; background-color: #73b270"></div>
                <div style="padding-left: 10px">Modest increase you're likely to hit</div>
            </div>
            <div style="display: flex; align-items: center">
                <div style="width: 12px; height: 12px; background-color: #9ac1ea"></div>
                <div style="padding-left: 10px">Equivalent to adding an extra rep</div>
            </div>
        </div>
    </div>
`)


    
const render = (state) => {
    const summary = figureItOut(state);
    return `
       ${renderSummary(state, summary)}
       <div style="max-width: 100%; overflow-x: auto; overflow-y: hidden">
          ${renderTable(state, summary)}
       </div>
       ${legend(state)}  
      `
}
    
const app = document.getElementById('app');

const updateDOM = (html) => {
  app.innerHTML = html
}

updateDOM(render(state.state))

document.getElementById("reps").addEventListener("input", e => {
  state.state = {...state.state, reps: Number(e.target.value)}
})

document.getElementById("weight").addEventListener("input", e => {
  state.state = {...state.state, weight: Number(e.target.value)}
})



