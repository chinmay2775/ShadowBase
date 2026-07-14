import { useState, useRef } from 'react'
import Editor from '@monaco-editor/react'


export default function SqlEditor() {
  const [output, setOutput] = useState('')
  const editorRef = useRef(null)

  function handleEditorMount(editor) {
    editorRef.current = editor
  }

  function handleRun() {
    const sql = editorRef.current.getValue()
    console.log('SQL to run:', sql)
    setOutput(sql)
  }

  return (
    <div className="sql-editor-card">
      <div className="sql-editor-header">
        <h2>Migration Script</h2>
        <button className="run-button" onClick={handleRun}>▶ Run</button>
      </div>

      <Editor
        height="240px"
        defaultLanguage="sql"
        defaultValue={'-- Write your migration SQL here\nALTER TABLE customers DROP COLUMN email;'}
        theme="vs-dark"
        onMount={handleEditorMount}
        options={{
          minimap: { enabled: false },
          fontSize: 14,
        }}
      />
      
      {output && (
        <div className="sql-output">
          <strong>Captured text:</strong>
          <pre>{output}</pre>
        </div>
      )}
    </div>
  )
}